/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.stream.app.python.shell;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.config.server.support.AbstractScmAccessor;
import org.springframework.cloud.stream.shell.ShellCommand;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Base class for {@link PythonAppDeployer} implementations. This will install dependent Python packages as specified
 * in 'requirements.txt' if this file exists.
 *
 * @author David Turanski
 **/
public abstract class AbstractPythonAppDeployer implements PythonAppDeployer {
	protected Log log = LogFactory.getLog(ClassPathPythonAppDeployer.class);

	private Resource appDir;
	private String pipCommandName = "pip";

	@Override
	public Resource getAppDir() {
		return this.appDir;
	}

	protected AbstractPythonAppDeployer() {
		File tempDirectory = null;
		try {
			tempDirectory = Files.createTempDirectory("python").toFile();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						FileUtils.cleanDirectory(appDir.getFile());
						FileUtils.deleteDirectory(appDir.getFile());
					}
					catch (IOException e) {
						log.warn("Unable to delete temp directory " + getAppDirPath());
					}
				}
			});
			FileUtils.forceDeleteOnExit(tempDirectory);
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		appDir = new FileSystemResource(tempDirectory.getAbsolutePath());
	}

	/**
	 * Override the default application directory
	 *
	 * @param appDir
	 */
	public void setAppDir(Resource appDir) {
		Assert.notNull(appDir, "'appDir' must not be null.");
		this.appDir = appDir;

	}

	public String getAppDirPath() {
		String fullPath = null;
		try {
			fullPath = appDir.getFile().getAbsolutePath();
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return fullPath;
	}

	/**
	 * Override the pip command name, e.g., 'pip3'.
	 *
	 * @param pipCommandName
	 */
	public void setPipCommandName(String pipCommandName) {
		Assert.hasLength(pipCommandName, "'pipCommandName' must contain text.");
		this.pipCommandName = pipCommandName;
	}

	@Override
	public void deploy() {
		try {
			doDeploy();
			installDependendentPackages();
		}
		catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	protected abstract void doDeploy() throws Exception;

	private void installDependendentPackages() throws Exception {

		File requirementsDotTxt = new File(
				StringUtils.join(new String[] { getAppDirPath(), "requirements.txt" }, File.separator));
		if (requirementsDotTxt.exists()) {
			ShellCommand installer = new ShellCommand(String.format(StringUtils.join(new String[] { pipCommandName,
					"install", "-r", requirementsDotTxt.getAbsolutePath() }, " ")));

			installer.afterPropertiesSet();
			installer.start();
			installer.stop();
		}
	}
}
