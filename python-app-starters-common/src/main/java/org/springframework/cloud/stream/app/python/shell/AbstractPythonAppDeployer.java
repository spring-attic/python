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

import com.sun.javafx.runtime.SystemProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.stream.shell.ShellCommand;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for {@link PythonAppDeployer} implementations. This will install dependent Python packages as specified
 * in 'requirements.txt' if this file exists.
 *
 * @author David Turanski
 **/
public abstract class AbstractPythonAppDeployer implements PythonAppDeployer {
	protected Log log = LogFactory.getLog(this.getClass());

	private FileSystemResource appDir;
	private String pipCommandName = "pip";

	protected AbstractPythonAppDeployer() {
		this(null);
	}

	/**
	 * @param appDir if null, a temporary directory will be created.
	 */
	protected AbstractPythonAppDeployer(FileSystemResource appDir) {
		if (appDir == null) {
			File tempDirectory = null;
			try {
				tempDirectory = Files.createTempDirectory("python").toFile();
				addDeleteShutdownHook(tempDirectory);
			}
			catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}

			appDir = new FileSystemResource(tempDirectory.getAbsolutePath());
		}
		this.appDir = appDir;
	}

	@Override
	public FileSystemResource getAppDir() {
		return this.appDir;
	}

	public String getAppDirPath() {
		return appDir.getFile().getAbsolutePath();
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

			String command = StringUtils.join(new String[] {
					pipCommandName, "install", "--user","-r", requirementsDotTxt.getAbsolutePath() },
					" ");

			String userPipInstallHome =  System.getenv("HOME") + File.separator +
					".local" + File.separator + "bin";

			if (new File(userPipInstallHome + File.separator + "pip").exists()) {
			    command = userPipInstallHome + File.separator + command;
			}

			ShellCommand installer = new ShellCommand(command);;

			if (log.isInfoEnabled()) {
				log.info(String.format("executing command -- [ %s ]" ,installer.getCommand()));
			}

			ShellCommand.CommandResponse response = installer.execute();

			if (log.isInfoEnabled()) {
				for (String line : StringUtils.split(response.output(), "\n")) {
					log.info("STDOUT " + line);
				}
				log.info("Command completed exit value " + response.exitValue());
			}
		}
	}

	private void addDeleteShutdownHook(final File dir) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					FileUtils.cleanDirectory(dir);
					FileUtils.deleteDirectory(dir);
				}
				catch (IOException e) {
					log.warn("Unable to delete temp directory " + getAppDirPath());
				}
			}
		});

	}
}
