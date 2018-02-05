/*
 * Copyright 2017-2018 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.springframework.cloud.stream.app.common.resource.repository;

import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.server.support.PassphraseCredentialsProvider;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK;
import static org.springframework.util.StringUtils.hasText;

/**
 * A Resource provider backed by a single git repository.
 *
 * @author Dave Syer
 * @author Roy Clarkson
 * @author Marcos Barbero
 * @author Daniel Lavoie
 * @author Ryan Lynch
 * @author David Turanski
 * @author Chris Schaefer
 */
@ConfigurationProperties("git")
public class JGitResourceRepository implements InitializingBean {

	private static final String FILE_URI_PREFIX = "file:";
	private static final String DEFAULT_LABEL = "master";
	private static final String DEFAULT_PREFIX = "resource_repo";

	protected Log logger = LogFactory.getLog(this.getClass());

	/**
	 * The base directory where the repository should be cloned. If not specified, a temporary directory will be
	 * created.
	 */
	private File basedir;

	/**
	 * The URI of the remote repository.
	 */
	private String uri;

	/**
	 * The username for the remote repository.
	 */
	private String username;

	/**
	 * The password for the remote repository.
	 */
	private String password;

	/**
	 * The passphrase for the remote repository.
	 */
	private String passphrase;

	private String prefix = DEFAULT_PREFIX;

	private boolean strictHostKeyChecking = true;

	/**
	 * Timeout (in seconds) for obtaining HTTP or SSH connection (if applicable). Default
	 * 5 seconds.
	 */
	private int timeout = 5;

	private boolean initialized;

	/**
	 * The label or branch to clone.
	 */
	private String label = DEFAULT_LABEL;

	/**
	 * Flag to indicate that the repository should be cloned on startup (not on demand).
	 * Generally leads to slower startup but faster first query.
	 */
	private boolean cloneOnStart = true;

	private JGitFactory gitFactory = new JGitFactory();

	/**
	 * The credentials provider to use to connect to the Git repository.
	 */
	private CredentialsProvider gitCredentialsProvider;

	/**
	 * Flag to indicate that the repository should force pull. If true discard any local
	 * changes and take from remote repository.
	 */
	private boolean forcePull;

	public boolean isCloneOnStart() {
		return this.cloneOnStart;
	}

	public void setCloneOnStart(boolean cloneOnStart) {
		this.cloneOnStart = cloneOnStart;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setBasedir(File basedir) {
		this.basedir = basedir.getAbsoluteFile();
	}

	public File getBasedir() {
		return this.basedir;
	}

	public void setUri(String uri) {
		while (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 1);
		}

		int index = uri.indexOf("://");
		if (index > 0 && !uri.substring(index + "://".length()).contains("/")) {
			uri = uri + "/";
		}

		this.uri = uri;
	}

	public String getUri() {
		return this.uri;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setStrictHostKeyChecking(boolean strictHostKeyChecking) {
		this.strictHostKeyChecking = strictHostKeyChecking;
	}

	public void setGitFactory(JGitFactory gitFactory) {
		this.gitFactory = gitFactory;
	}

	public void setForcePull(boolean forcePull) {
		this.forcePull = forcePull;
	}

	/**
	 * @param gitCredentialsProvider the gitCredentialsProvider to set
	 */
	public void setGitCredentialsProvider(CredentialsProvider gitCredentialsProvider) {
		this.gitCredentialsProvider = gitCredentialsProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(getUri() != null, "You need to configure a uri for the git repository");
		initialize();
		if (this.cloneOnStart) {
			initClonedRepository();
		}
	}

	/**
	 * Get the working directory ready.
	 */
	public String refresh(String branch) {
		initialize();
		Git git = null;
		try {
			git = createGitClient();
			if (shouldPull(git)) {
				fetch(git, branch);
				//checkout after fetch so we can get any new branches, tags, ect.
				checkout(git, branch);
				if (isBranch(git, branch)) {
					//merge results from fetch
					merge(git, branch);
					if (!isClean(git)) {
						logger.warn("The local repository is dirty. Resetting it to origin/" + branch + ".");
						resetHard(git, branch, "refs/remotes/origin/" + branch);
					}
				}
			}
			else {
				//nothing to update so just checkout
				checkout(git, branch);
			}
			//always return what is currently HEAD as the version
			return git.getRepository().findRef("HEAD").getObjectId().getName();
		}
		catch (RefNotFoundException e) {
			throw new NoSuchBranchException("No such branch: " + branch, e);
		}
		catch (GitAPIException e) {
			throw new IllegalStateException("Cannot clone or checkout repository", e);
		}
		catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		finally {
			try {
				if (git != null) {
					git.close();
				}
			}
			catch (Exception e) {
				this.logger.warn("Could not close git repository", e);
			}
		}
	}

	public void refresh() {
		refresh(this.label);
	}

	protected boolean isStrictHostKeyChecking() {
		return strictHostKeyChecking;
	}

	/**
	 * Clones the remote repository and then opens a connection to it.
	 *
	 * @throws GitAPIException
	 * @throws IOException
	 */
	private void initClonedRepository() throws GitAPIException, IOException {
		if (!getUri().startsWith(FILE_URI_PREFIX)) {
			deleteBaseDirIfExists();
			Git git = cloneToBasedir();
			if (git != null) {
				git.close();
			}
			git = openGitRepository();
			if (git != null) {
				git.close();
			}
		}

	}

	private Ref checkout(Git git, String branch) throws GitAPIException {
		CheckoutCommand checkout = git.checkout();
		if (shouldTrack(git, branch)) {
			trackBranch(git, checkout, branch);
		}
		else {
			// works for tags and local branches
			checkout.setName(branch);
		}
		return checkout.call();
	}

	public /*public for testing*/ boolean shouldPull(Git git) throws GitAPIException {
		boolean shouldPull;
		Status gitStatus = git.status().call();
		boolean isWorkingTreeClean = gitStatus.isClean();
		String originUrl = git.getRepository().getConfig().getString("remote", "origin", "url");

		if (this.forcePull && !isWorkingTreeClean) {
			shouldPull = true;
			logDirty(gitStatus);
		}
		else {
			shouldPull = isWorkingTreeClean && originUrl != null;
		}
		if (!isWorkingTreeClean && !this.forcePull) {
			this.logger.info("Cannot pull from remote " + originUrl + ", the working tree is not clean.");
		}
		return shouldPull;
	}

	@SuppressWarnings("unchecked")
	private void logDirty(Status status) {
		Set<String> dirties = dirties(status.getAdded(), status.getChanged(), status.getRemoved(), status.getMissing(),
				status.getModified(), status.getConflicting(), status.getUntracked());
		this.logger.warn(String.format("Dirty files found: %s", dirties));
	}

	@SuppressWarnings("unchecked")
	private Set<String> dirties(Set<String>... changes) {
		Set<String> dirties = new HashSet<>();
		for (Set<String> files : changes) {
			dirties.addAll(files);
		}
		return dirties;
	}

	private boolean shouldTrack(Git git, String branch) throws GitAPIException {
		return isBranch(git, branch) && !isLocalBranch(git, branch);
	}

	private FetchResult fetch(Git git, String branch) {
		FetchCommand fetch = git.fetch();
		fetch.setRemote("origin");
		fetch.setTagOpt(TagOpt.FETCH_TAGS);

		setTimeout(fetch);
		try {
			setCredentialsProvider(fetch);
			FetchResult result = fetch.call();
			if (result.getTrackingRefUpdates() != null && result.getTrackingRefUpdates().size() > 0) {
				logger.info("Fetched for remote " + branch + " and found " + result.getTrackingRefUpdates().size()
						+ " updates");
			}
			return result;
		}
		catch (Exception ex) {
			String message = "Could not fetch remote for " + branch + " remote: " + git.getRepository().getConfig()
					.getString("remote", "origin", "url");
			warn(message, ex);
			return null;
		}
	}

	private MergeResult merge(Git git, String branch) {
		try {
			MergeCommand merge = git.merge();
			merge.include(git.getRepository().findRef("origin/" + branch));
			MergeResult result = merge.call();
			if (!result.getMergeStatus().isSuccessful()) {
				this.logger.warn("Merged from remote " + branch + " with result " + result.getMergeStatus());
			}
			return result;
		}
		catch (Exception ex) {
			String message = "Could not merge remote for " + branch + " remote: " + git.getRepository().getConfig()
					.getString("remote", "origin", "url");
			warn(message, ex);
			return null;
		}
	}

	private Ref resetHard(Git git, String branch, String ref) {
		ResetCommand reset = git.reset();
		reset.setRef(ref);
		reset.setMode(ResetCommand.ResetType.HARD);
		try {
			Ref resetRef = reset.call();
			if (resetRef != null) {
				this.logger.info("Reset branch " + branch + " to version " + resetRef.getObjectId());
			}
			return resetRef;
		}
		catch (Exception ex) {
			String message = "Could not reset to remote for " + branch + " (current ref=" + ref + "), remote: " + git
					.getRepository().getConfig().getString("remote", "origin", "url");
			warn(message, ex);
			return null;
		}
	}

	private Git createGitClient() throws IOException, GitAPIException {
		if (new File(getBasedir(), ".git").exists()) {
			return openGitRepository();
		}
		else {
			return copyRepository();
		}
	}

	// Synchronize here so that multiple requests don't all try and delete the base dir
	// together (this is a once only operation, so it only holds things up on the first
	// request).
	private synchronized Git copyRepository() throws IOException, GitAPIException {
		deleteBaseDirIfExists();
		getBasedir().mkdirs();
		Assert.state(getBasedir().exists(), "Could not create basedir: " + getBasedir());
		if (getUri().startsWith(FILE_URI_PREFIX)) {
			return copyFromLocalRepository();
		}
		else {
			return cloneToBasedir();
		}
	}

	private Git openGitRepository() throws IOException {
		Git git = this.gitFactory.getGitByOpen(getWorkingDirectory());
		return git;
	}

	private File getWorkingDirectory() {
		if (this.uri.startsWith("file:")) {
			try {
				return (new UrlResource(StringUtils.cleanPath(this.uri))).getFile();
			}
			catch (Exception var2) {
				throw new IllegalStateException("Cannot convert uri to file: " + this.uri);
			}
		}
		else {
			return this.basedir;
		}
	}

	private Git copyFromLocalRepository() throws IOException {
		Git git;
		File remote = new UrlResource(StringUtils.cleanPath(getUri())).getFile();
		Assert.state(remote.isDirectory(), "No directory at " + getUri());
		File gitDir = new File(remote, ".git");
		Assert.state(gitDir.exists(), "No .git at " + getUri());
		Assert.state(gitDir.isDirectory(), "No .git directory at " + getUri());
		git = this.gitFactory.getGitByOpen(remote);
		return git;
	}

	private Git cloneToBasedir() throws GitAPIException {
		CloneCommand clone = this.gitFactory.getCloneCommandByCloneRepository().setURI(getUri())
				.setDirectory(getBasedir());
		setTimeout(clone);
		setCredentialsProvider(clone);
		try {
			return clone.call();
		}
		catch (GitAPIException e) {
			deleteBaseDirIfExists();
			throw e;
		}
	}

	private void deleteBaseDirIfExists() {
		if (this.basedir.exists()) {
			try {
				FileUtils.delete(getBasedir(), FileUtils.RECURSIVE);
			}
			catch (IOException e) {
				throw new IllegalStateException("Failed to initialize base directory", e);
			}
		}
	}

	private void initialize() {
		if (!this.initialized) {
			this.basedir = createBaseDir();
			SshSessionFactory.setInstance(new JschConfigSessionFactory() {
				@Override
				protected void configure(Host hc, Session session) {
					session.setConfig("StrictHostKeyChecking", strictHostKeyChecking ? "yes" : "no");
				}
			});
			this.initialized = true;
		}
	}

	private File createBaseDir() {
		try {
			final File dir = Files.createTempDirectory(prefix).toFile();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					if (!dir.exists()) {
						return;
					}
					try {
						FileUtils.delete(dir, FileUtils.RECURSIVE);
					}
					catch (IOException e) {
						logger.warn("Failed to delete temporary directory on exit: " + e);
					}
				}
			});
			return dir;
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot create temp dir", e);
		}
	}

	private void setCredentialsProvider(TransportCommand<?, ?> cmd) {
		if (gitCredentialsProvider != null) {
			cmd.setCredentialsProvider(gitCredentialsProvider);
		}
		else if (hasText(username)) {
			cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
		}
		else if (hasText(passphrase)) {
			cmd.setCredentialsProvider(new PassphraseCredentialsProvider(passphrase));
		}
	}

	private void setTimeout(TransportCommand<?, ?> pull) {
		pull.setTimeout(this.timeout);
	}

	private boolean isClean(Git git) {
		StatusCommand status = git.status();
		try {
			return status.call().isClean();
		}
		catch (Exception e) {
			String message =
					"Could not execute status command on local repository. Cause: (" + e.getClass().getSimpleName()
							+ ") " + e.getMessage();
			warn(message, e);
			return false;
		}
	}

	private void trackBranch(Git git, CheckoutCommand checkout, String branch) {
		checkout.setCreateBranch(true).setName(branch).setUpstreamMode(TRACK).setStartPoint("origin/" + branch);
	}

	private boolean isBranch(Git git, String branch) throws GitAPIException {
		return containsBranch(git, branch, ListMode.ALL);
	}

	private boolean isLocalBranch(Git git, String branch) throws GitAPIException {
		return containsBranch(git, branch, null);
	}

	private boolean containsBranch(Git git, String branch, ListMode listMode) throws GitAPIException {
		ListBranchCommand command = git.branchList();
		if (listMode != null) {
			command.setListMode(listMode);
		}
		List<Ref> branches = command.call();
		for (Ref ref : branches) {
			if (ref.getName().endsWith("/" + branch)) {
				return true;
			}
		}
		return false;
	}

	protected void warn(String message, Exception ex) {
		logger.warn(message);
		if (logger.isDebugEnabled()) {
			logger.debug("Stacktrace for: " + message, ex);
		}
	}

	/**
	 * Wraps the static method calls to {@link org.eclipse.jgit.api.Git} and
	 * {@link org.eclipse.jgit.api.CloneCommand} allowing for easier unit testing.
	 */
	static class JGitFactory {

		public Git getGitByOpen(File file) throws IOException {
			Git git = Git.open(file);
			return git;
		}

		public CloneCommand getCloneCommandByCloneRepository() {
			CloneCommand command = Git.cloneRepository();
			return command;
		}
	}
}
