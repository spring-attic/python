/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.common.resource.repository;

import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.config.server.support.AwsCodeCommitCredentialProvider;
import org.springframework.cloud.config.server.support.GitCredentialsProviderFactory;
import org.springframework.cloud.config.server.support.PassphraseCredentialsProvider;
import org.springframework.cloud.config.server.test.ConfigServerTestUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dave Syer
 * @author David Turanski
 */
public class JGitResourceRepositoryTests {

	private JGitResourceRepository repository = new JGitResourceRepository();

	private File basedir = new File("target/resources");

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void init() throws Exception {
		if (this.basedir.exists()) {
			FileUtils.delete(this.basedir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
	}

	@Test
	public void appLocal() throws Exception {
		String uri = ConfigServerTestUtils.prepareLocalRepo("my-python-app");
		System.out.println("uri:" + uri);
		this.repository.setCloneOnStart(false);
		this.repository.setUri(uri);
		this.repository.afterPropertiesSet();
		File baseDir = this.repository.getBasedir();
		String result = this.repository.refresh("master");
		assertTrue(this.repository.getBasedir().exists());
		assertFalse(new File(this.repository.getBasedir().getAbsolutePath() + "/.git").exists());
	}

	@Test
	public void branch() throws Exception {
		String uri = ConfigServerTestUtils.prepareLocalRepo("my-python-app");
		this.repository.setUri(uri);
		this.repository.setBasedir(this.basedir);
		this.repository.afterPropertiesSet();
		this.repository.refresh("bar");

	}

	@Test(expected = NoSuchBranchException.class)
	public void branchDoeNotExist() throws Exception {
		String uri = ConfigServerTestUtils.prepareLocalRepo("my-python-app");
		this.repository.setUri(uri);
		this.repository.setBasedir(this.basedir);
		this.repository.afterPropertiesSet();
		this.repository.refresh("foo");

	}

	@Test
	public void basedirExists() throws Exception {
		assertTrue(this.basedir.mkdirs());
		assertTrue(new File(this.basedir, ".nothing").createNewFile());

	}

	@Test
	public void uriWithHostOnly() throws Exception {
		this.repository.setUri("git://localhost");
		assertEquals("git://localhost/", this.repository.getUri());
	}

	@Test
	public void uriWithHostAndPath() throws Exception {
		this.repository.setUri("git://localhost/foo/");
		assertEquals("git://localhost/foo", this.repository.getUri());
	}

	@Test
	public void afterPropertiesSet_CloneOnStartTrue_CloneAndFetchCalled() throws Exception {
		Git mockGit = mock(Git.class);
		CloneCommand mockCloneCommand = mock(CloneCommand.class);

		when(mockCloneCommand.setURI(anyString())).thenReturn(mockCloneCommand);
		when(mockCloneCommand.setDirectory(any(File.class))).thenReturn(mockCloneCommand);

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri("http://somegitserver/somegitrepo");
		resourceRepository.setCloneOnStart(true);
		resourceRepository.afterPropertiesSet();
		verify(mockCloneCommand, times(1)).call();
	}

	@Test
	public void afterPropertiesSet_CloneOnStartFalse_CloneAndFetchNotCalled() throws Exception {
		Git mockGit = mock(Git.class);
		CloneCommand mockCloneCommand = mock(CloneCommand.class);

		when(mockCloneCommand.setURI(anyString())).thenReturn(mockCloneCommand);
		when(mockCloneCommand.setDirectory(any(File.class))).thenReturn(mockCloneCommand);

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setCloneOnStart(false);
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri("http://somegitserver/somegitrepo");
		resourceRepository.afterPropertiesSet();
		verify(mockCloneCommand, times(0)).call();
		verify(mockGit, times(0)).fetch();
	}

	@Test
	public void afterPropertiesSet_CloneOnStartTrueWithFileURL_CloneAndFetchNotCalled() throws Exception {
		Git mockGit = mock(Git.class);
		CloneCommand mockCloneCommand = mock(CloneCommand.class);

		when(mockCloneCommand.setURI(anyString())).thenReturn(mockCloneCommand);
		when(mockCloneCommand.setDirectory(any(File.class))).thenReturn(mockCloneCommand);

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setCloneOnStart(false);
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri("file://somefilesystem/somegitrepo");
		resourceRepository.setCloneOnStart(true);
		resourceRepository.afterPropertiesSet();
		verify(mockCloneCommand, times(0)).call();
		verify(mockGit, times(0)).fetch();
	}

	@Test
	public void shouldPullForcepullNotClean() throws Exception {
		Git git = mock(Git.class);
		StatusCommand statusCommand = mock(StatusCommand.class);
		Status status = mock(Status.class);
		Repository repository = mock(Repository.class);
		StoredConfig storedConfig = mock(StoredConfig.class);

		when(git.status()).thenReturn(statusCommand);
		when(git.getRepository()).thenReturn(repository);
		when(repository.getConfig()).thenReturn(storedConfig);
		when(storedConfig.getString("remote", "origin", "url")).thenReturn("http://example/git");
		when(statusCommand.call()).thenReturn(status);
		when(status.isClean()).thenReturn(false);

		JGitResourceRepository repo = new JGitResourceRepository();
		repo.setForcePull(true);

		boolean shouldPull = repo.shouldPull(git);

		assertThat("shouldPull was false", shouldPull, is(true));
	}

	@Test
	public void shouldPullNotClean() throws Exception {
		Git git = mock(Git.class);
		StatusCommand statusCommand = mock(StatusCommand.class);
		Status status = mock(Status.class);
		Repository repository = mock(Repository.class);
		StoredConfig storedConfig = mock(StoredConfig.class);

		when(git.status()).thenReturn(statusCommand);
		when(git.getRepository()).thenReturn(repository);
		when(repository.getConfig()).thenReturn(storedConfig);
		when(storedConfig.getString("remote", "origin", "url")).thenReturn("http://example/git");
		when(statusCommand.call()).thenReturn(status);
		when(status.isClean()).thenReturn(false);

		JGitResourceRepository repo = new JGitResourceRepository();

		boolean shouldPull = repo.shouldPull(git);

		assertThat("shouldPull was true", shouldPull, is(false));
	}

	@Test
	public void shouldPullClean() throws Exception {
		Git git = mock(Git.class);
		StatusCommand statusCommand = mock(StatusCommand.class);
		Status status = mock(Status.class);
		Repository repository = mock(Repository.class);
		StoredConfig storedConfig = mock(StoredConfig.class);

		when(git.status()).thenReturn(statusCommand);
		when(git.getRepository()).thenReturn(repository);
		when(repository.getConfig()).thenReturn(storedConfig);
		when(storedConfig.getString("remote", "origin", "url")).thenReturn("http://example/git");
		when(statusCommand.call()).thenReturn(status);
		when(status.isClean()).thenReturn(true);

		JGitResourceRepository repo = new JGitResourceRepository();

		boolean shouldPull = repo.shouldPull(git);

		assertThat("shouldPull was false", shouldPull, is(true));
	}

	@Test
	public void testFetchException() throws Exception {
		Git git = mock(Git.class);
		CloneCommand cloneCommand = mock(CloneCommand.class);
		MockGitFactory factory = new MockGitFactory(git, cloneCommand);
		when(cloneCommand.setURI(anyString())).thenReturn(cloneCommand);
		when(cloneCommand.setDirectory(any(File.class))).thenReturn(cloneCommand);
		when(cloneCommand.call()).thenReturn(git);

		//refresh()->shouldPull
		StatusCommand statusCommand = mock(StatusCommand.class);
		Status status = mock(Status.class);
		when(git.status()).thenReturn(statusCommand);
		Repository repository = mock(Repository.class);
		when(git.getRepository()).thenReturn(repository);
		StoredConfig storedConfig = mock(StoredConfig.class);
		when(repository.getConfig()).thenReturn(storedConfig);
		when(storedConfig.getString("remote", "origin", "url")).thenReturn("http://example/git");
		when(statusCommand.call()).thenReturn(status);
		when(status.isClean()).thenReturn(true);

		//refresh()->fetch
		FetchCommand fetchCommand = mock(FetchCommand.class);
		when(git.fetch()).thenReturn(fetchCommand);
		when(fetchCommand.setRemote(anyString())).thenReturn(fetchCommand);
		when(fetchCommand.call()).thenThrow(new InvalidRemoteException("invalid mock remote")); //here is our exception we are testing

		//refresh()->checkout
		CheckoutCommand checkoutCommand = mock(CheckoutCommand.class);
		//refresh()->checkout->containsBranch
		ListBranchCommand listBranchCommand = mock(ListBranchCommand.class);
		when(git.checkout()).thenReturn(checkoutCommand);
		when(git.branchList()).thenReturn(listBranchCommand);
		List<Ref> refs = new ArrayList<>();
		Ref ref = mock(Ref.class);
		refs.add(ref);
		when(ref.getName()).thenReturn("/master");
		when(listBranchCommand.call()).thenReturn(refs);

		//refresh()->merge
		MergeCommand mergeCommand = mock(MergeCommand.class);
		when(git.merge()).thenReturn(mergeCommand);
		when(mergeCommand.call()).thenThrow(new NotMergedException()); //here is our exception we are testing

		//refresh()->return git.getRepository().getRef("HEAD").getObjectId().getName();
		Ref headRef = mock(Ref.class);
		when(repository.getRef(anyString())).thenReturn(headRef);

		ObjectId newObjectId = ObjectId.fromRaw(new int[]{1,2,3,4,5});
		when(headRef.getObjectId()).thenReturn(newObjectId);

		this.repository.setGitFactory(factory);
		this.repository.setUri("http://example/git");
		this.repository.setCloneOnStart(false);
		this.repository.afterPropertiesSet();

		String version = null;
		try {
			version = this.repository.refresh("master");
		}
		catch (Exception e) {

		}
		assertEquals(version, newObjectId.getName());
	}

	@Test
	public void testMergeException() throws Exception {
		Git git = mock(Git.class);
		CloneCommand cloneCommand = mock(CloneCommand.class);
		MockGitFactory factory = new MockGitFactory(git, cloneCommand);
		when(cloneCommand.setURI(anyString())).thenReturn(cloneCommand);
		when(cloneCommand.setDirectory(any(File.class))).thenReturn(cloneCommand);
		when(cloneCommand.call()).thenReturn(git);

		//refresh()->shouldPull
		StatusCommand statusCommand = mock(StatusCommand.class);
		Status status = mock(Status.class);
		when(git.status()).thenReturn(statusCommand);
		Repository repository = mock(Repository.class);
		when(git.getRepository()).thenReturn(repository);
		StoredConfig storedConfig = mock(StoredConfig.class);
		when(repository.getConfig()).thenReturn(storedConfig);
		when(storedConfig.getString("remote", "origin", "url")).thenReturn("http://example/git");
		when(statusCommand.call()).thenReturn(status);
		when(status.isClean()).thenReturn(true);

		//refresh()->fetch
		FetchCommand fetchCommand = mock(FetchCommand.class);
		FetchResult fetchResult = mock(FetchResult.class);
		when(git.fetch()).thenReturn(fetchCommand);
		when(fetchCommand.setRemote(anyString())).thenReturn(fetchCommand);
		when(fetchCommand.call()).thenReturn(fetchResult);
		when(fetchResult.getTrackingRefUpdates()).thenReturn(Collections.EMPTY_LIST);

		//refresh()->checkout
		CheckoutCommand checkoutCommand = mock(CheckoutCommand.class);
		//refresh()->checkout->containsBranch
		ListBranchCommand listBranchCommand = mock(ListBranchCommand.class);
		when(git.checkout()).thenReturn(checkoutCommand);
		when(git.branchList()).thenReturn(listBranchCommand);
		List<Ref> refs = new ArrayList<>();
		Ref ref = mock(Ref.class);
		refs.add(ref);
		when(ref.getName()).thenReturn("/master");
		when(listBranchCommand.call()).thenReturn(refs);

		//refresh()->merge
		MergeCommand mergeCommand = mock(MergeCommand.class);
		when(git.merge()).thenReturn(mergeCommand);
		when(mergeCommand.call()).thenThrow(new NotMergedException()); //here is our exception we are testing

		//refresh()->return git.getRepository().getRef("HEAD").getObjectId().getName();
		Ref headRef = mock(Ref.class);
		when(repository.getRef(anyString())).thenReturn(headRef);

		ObjectId newObjectId = ObjectId.fromRaw(new int[]{1,2,3,4,5});
		when(headRef.getObjectId()).thenReturn(newObjectId);

		this.repository.setGitFactory(factory);
		this.repository.setUri("http://example/git");
		this.repository.setCloneOnStart(false);
		this.repository.afterPropertiesSet();

		String version = null;
		try {
			version = this.repository.refresh("master");
		}
		catch (Exception e) {

		}
		assertEquals(version, newObjectId.getName());


	}

	@Test
	public void testResetHardException() throws Exception {

		Git git = mock(Git.class);
		CloneCommand cloneCommand = mock(CloneCommand.class);
		MockGitFactory factory = new MockGitFactory(git, cloneCommand);
		when(cloneCommand.setURI(anyString())).thenReturn(cloneCommand);
		when(cloneCommand.setDirectory(any(File.class))).thenReturn(cloneCommand);
		when(cloneCommand.call()).thenReturn(git);

		//refresh()->shouldPull
		StatusCommand statusCommand = mock(StatusCommand.class);
		Status status = mock(Status.class);
		when(git.status()).thenReturn(statusCommand);
		Repository repository = mock(Repository.class);
		when(git.getRepository()).thenReturn(repository);
		StoredConfig storedConfig = mock(StoredConfig.class);
		when(repository.getConfig()).thenReturn(storedConfig);
		when(storedConfig.getString("remote", "origin", "url")).thenReturn("http://example/git");
		when(statusCommand.call()).thenReturn(status);
		when(status.isClean()).thenReturn(true).thenReturn(false);

		//refresh()->fetch
		FetchCommand fetchCommand = mock(FetchCommand.class);
		FetchResult fetchResult = mock(FetchResult.class);
		when(git.fetch()).thenReturn(fetchCommand);
		when(fetchCommand.setRemote(anyString())).thenReturn(fetchCommand);
		when(fetchCommand.call()).thenReturn(fetchResult);
		when(fetchResult.getTrackingRefUpdates()).thenReturn(Collections.EMPTY_LIST);

		//refresh()->checkout
		CheckoutCommand checkoutCommand = mock(CheckoutCommand.class);
		//refresh()->checkout->containsBranch
		ListBranchCommand listBranchCommand = mock(ListBranchCommand.class);
		when(git.checkout()).thenReturn(checkoutCommand);
		when(git.branchList()).thenReturn(listBranchCommand);
		List<Ref> refs = new ArrayList<>();
		Ref ref = mock(Ref.class);
		refs.add(ref);
		when(ref.getName()).thenReturn("/master");
		when(listBranchCommand.call()).thenReturn(refs);

		//refresh()->merge
		MergeCommand mergeCommand = mock(MergeCommand.class);
		when(git.merge()).thenReturn(mergeCommand);
		when(mergeCommand.call()).thenThrow(new NotMergedException()); //here is our exception we are testing

		//refresh()->hardReset
		ResetCommand resetCommand = mock(ResetCommand.class);
		when(git.reset()).thenReturn(resetCommand);
		when(resetCommand.call()).thenReturn(ref);

		//refresh()->return git.getRepository().getRef("HEAD").getObjectId().getName();
		Ref headRef = mock(Ref.class);
		when(repository.getRef(anyString())).thenReturn(headRef);

		ObjectId newObjectId = ObjectId.fromRaw(new int[] { 1, 2, 3, 4, 5 });
		when(headRef.getObjectId()).thenReturn(newObjectId);

		this.repository.setGitFactory(factory);
		this.repository.setUri("http://example/git");
		this.repository.setCloneOnStart(false);
		this.repository.afterPropertiesSet();

		String version = null;
		try {
			version = this.repository.refresh("master");
		}
		catch (Exception e) {

		}
		assertEquals(version, newObjectId.getName());
	}

	@Test
	public void shouldDeleteBaseDirWhenCloneFails() throws Exception {
		Git mockGit = mock(Git.class);
		CloneCommand mockCloneCommand = mock(CloneCommand.class);

		when(mockCloneCommand.setURI(anyString())).thenReturn(mockCloneCommand);
		when(mockCloneCommand.setDirectory(any(File.class))).thenReturn(mockCloneCommand);
		when(mockCloneCommand.call()).thenThrow(new TransportException("failed to clone"));

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri("http://somegitserver/somegitrepo");
		resourceRepository.setBasedir(this.basedir);
		resourceRepository.setCloneOnStart(false);
		resourceRepository.afterPropertiesSet();


		try {
			resourceRepository.refresh("master");
		}
		catch (Exception ex) {
			// expected - ignore
		}

		assertFalse("baseDir should be deleted when clone fails", this.basedir.exists());
	}

	@Test
	public void usernamePasswordShouldSetCredentials() throws Exception {
		Git mockGit = mock(Git.class);
		MockCloneCommand mockCloneCommand = new MockCloneCommand(mockGit);

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri("git+ssh://git@somegitserver/somegitrepo");
		resourceRepository.setBasedir(new File("./mybasedir"));
		final String username = "someuser";
		final String password = "mypassword";
		resourceRepository.setUsername(username);
		resourceRepository.setPassword(password);
		resourceRepository.setCloneOnStart(true);
		resourceRepository.afterPropertiesSet();

		assertTrue(mockCloneCommand.getCredentialsProvider() instanceof UsernamePasswordCredentialsProvider);

		CredentialsProvider provider = mockCloneCommand.getCredentialsProvider();
		CredentialItem.Username usernameCredential = new CredentialItem.Username();
		CredentialItem.Password passwordCredential = new CredentialItem.Password();
		assertTrue(provider.supports(usernameCredential));
		assertTrue(provider.supports(passwordCredential));

		provider.get(new URIish(), usernameCredential);
		assertEquals(usernameCredential.getValue(), username);
		provider.get(new URIish(), passwordCredential);
		assertEquals(String.valueOf(passwordCredential.getValue()), password);
	}

	@Test
	public void passphraseShouldSetCredentials() throws Exception {
		final String passphrase = "mypassphrase";
		Git mockGit = mock(Git.class);
		MockCloneCommand mockCloneCommand = new MockCloneCommand(mockGit);

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri("git+ssh://git@somegitserver/somegitrepo");
		resourceRepository.setBasedir(new File("./mybasedir"));
		resourceRepository.setPassphrase(passphrase);
		resourceRepository.setCloneOnStart(true);
		resourceRepository.afterPropertiesSet();

		assertTrue(mockCloneCommand.hasPassphraseCredentialsProvider());

		CredentialsProvider provider = mockCloneCommand.getCredentialsProvider();
		assertFalse(provider.isInteractive());

		CredentialItem.StringType stringCredential = new CredentialItem.StringType(PassphraseCredentialsProvider.PROMPT,
				true);

		assertTrue(provider.supports(stringCredential));
		provider.get(new URIish(), stringCredential);
		assertEquals(stringCredential.getValue(), passphrase);
	}

	@Test
	public void gitCredentialsProviderFactoryCreatesPassphraseProvider() throws Exception {
		final String passphrase = "mypassphrase";
		final String gitUri = "git+ssh://git@somegitserver/somegitrepo";
		GitCredentialsProviderFactory credentialsFactory = new GitCredentialsProviderFactory();
		Git mockGit = mock(Git.class);
		MockCloneCommand mockCloneCommand = new MockCloneCommand(mockGit);

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri(gitUri);
		resourceRepository.setBasedir(new File("./mybasedir"));
		resourceRepository.setGitCredentialsProvider(credentialsFactory.createFor(gitUri, null, null, passphrase));
		resourceRepository.setCloneOnStart(true);
		resourceRepository.afterPropertiesSet();

		assertTrue(mockCloneCommand.hasPassphraseCredentialsProvider());

		CredentialsProvider provider = mockCloneCommand.getCredentialsProvider();
		assertFalse(provider.isInteractive());

		CredentialItem.StringType stringCredential = new CredentialItem.StringType(PassphraseCredentialsProvider.PROMPT,
				true);

		assertTrue(provider.supports(stringCredential));
		provider.get(new URIish(), stringCredential);
		assertEquals(stringCredential.getValue(), passphrase);

	}

	@Test
	public void gitCredentialsProviderFactoryCreatesUsernamePasswordProvider() throws Exception {
		GitCredentialsProviderFactory credentialsFactory = new GitCredentialsProviderFactory();
		Git mockGit = mock(Git.class);
		MockCloneCommand mockCloneCommand = new MockCloneCommand(mockGit);
		final String username = "someuser";
		final String password = "mypassword";

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri("git+ssh://git@somegitserver/somegitrepo");
		resourceRepository.setBasedir(new File("./mybasedir"));
		resourceRepository.setGitCredentialsProvider(
				credentialsFactory.createFor(resourceRepository.getUri(), username, password, null));
		resourceRepository.setCloneOnStart(true);
		resourceRepository.afterPropertiesSet();

		assertTrue(mockCloneCommand.getCredentialsProvider() instanceof UsernamePasswordCredentialsProvider);

		CredentialsProvider provider = mockCloneCommand.getCredentialsProvider();
		CredentialItem.Username usernameCredential = new CredentialItem.Username();
		CredentialItem.Password passwordCredential = new CredentialItem.Password();
		assertTrue(provider.supports(usernameCredential));
		assertTrue(provider.supports(passwordCredential));

		provider.get(new URIish(), usernameCredential);
		assertEquals(usernameCredential.getValue(), username);
		provider.get(new URIish(), passwordCredential);
		assertEquals(String.valueOf(passwordCredential.getValue()), password);
	}

	@Test
	public void gitCredentialsProviderFactoryCreatesAwsCodeCommitProvider() throws Exception {
		GitCredentialsProviderFactory credentialsFactory = new GitCredentialsProviderFactory();
		Git mockGit = mock(Git.class);
		MockCloneCommand mockCloneCommand = new MockCloneCommand(mockGit);
		final String awsUri = "https://git-codecommit.us-east-1.amazonaws.com/v1/repos/test";

		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setGitFactory(new MockGitFactory(mockGit, mockCloneCommand));
		resourceRepository.setUri(awsUri);
		resourceRepository
				.setGitCredentialsProvider(credentialsFactory.createFor(resourceRepository.getUri(), null, null, null));
		resourceRepository.setCloneOnStart(true);
		resourceRepository.afterPropertiesSet();

		assertTrue(mockCloneCommand.getCredentialsProvider() instanceof AwsCodeCommitCredentialProvider);

	}

	@Test
	public void strictHostKeyCheckShouldCheck() throws Exception {
		String uri = "git+ssh://git@somegitserver/somegitrepo";
		SshSessionFactory.setInstance(null);
		JGitResourceRepository resourceRepository = new JGitResourceRepository();
		resourceRepository.setUri(uri);
		resourceRepository.setBasedir(new File("./mybasedir"));
		assertTrue(resourceRepository.isStrictHostKeyChecking());
		resourceRepository.setCloneOnStart(true);
		try {
			// this will throw but we don't care about connecting.
			resourceRepository.afterPropertiesSet();
		}
		catch (Exception e) {
			final OpenSshConfig.Host hc = OpenSshConfig.get(FS.detect()).lookup("github.com");
			JschConfigSessionFactory factory = (JschConfigSessionFactory) SshSessionFactory.getInstance();
			// There's no public method that can be used to inspect the ssh configuration, so we'll reflect
			// the configure method to allow us to check that the config property is set as expected.
			Method configure = factory.getClass()
					.getDeclaredMethod("configure", OpenSshConfig.Host.class, Session.class);
			configure.setAccessible(true);
			Session session = mock(Session.class);
			ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
			configure.invoke(factory, hc, session);
			verify(session).setConfig(keyCaptor.capture(), valueCaptor.capture());
			configure.setAccessible(false);
			assertTrue("yes".equals(valueCaptor.getValue()));
		}
	}

	@Test
	public void shouldPrintStacktraceIfDebugEnabled() throws Exception {
		final Log mockLogger = mock(Log.class);
		JGitResourceRepository resourceRepository = new JGitResourceRepository() {
			@Override
			public void afterPropertiesSet() throws Exception {
				this.logger = mockLogger;
			}
		};
		resourceRepository.afterPropertiesSet();
		when(mockLogger.isDebugEnabled()).thenReturn(true);

		resourceRepository.warn("", new RuntimeException());

		verify(mockLogger).warn(eq(""));
		verify(mockLogger).debug(eq("Stacktrace for: "), any(RuntimeException.class));

		int numberOfInvocations = mockingDetails(mockLogger).getInvocations().size();
		assertEquals("should call isDebugEnabled warn and debug", 3, numberOfInvocations);
	}

	class MockCloneCommand extends CloneCommand {
		private Git mockGit;

		public MockCloneCommand(Git mockGit) {
			this.mockGit = mockGit;
		}

		@Override
		public Git call() throws GitAPIException, InvalidRemoteException {
			return mockGit;
		}

		public boolean hasPassphraseCredentialsProvider() {
			return credentialsProvider instanceof PassphraseCredentialsProvider;
		}

		public CredentialsProvider getCredentialsProvider() {
			return credentialsProvider;
		}
	}

	class MockGitFactory extends JGitResourceRepository.JGitFactory {

		private Git mockGit;
		private CloneCommand mockCloneCommand;

		public MockGitFactory(Git mockGit, CloneCommand mockCloneCommand) {
			this.mockGit = mockGit;
			this.mockCloneCommand = mockCloneCommand;
		}

		@Override
		public Git getGitByOpen(File file) throws IOException {
			return this.mockGit;
		}

		@Override
		public CloneCommand getCloneCommandByCloneRepository() {
			return this.mockCloneCommand;
		}
	}
}
