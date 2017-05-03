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

package org.springframework.cloud.stream.shell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates a process to sendAndRecieve a shell command and communicate with it using String payloads over stdin and stdout.
 *
 * @author David Turanski
 * @author Gary Russell
 */
public class ShellCommand implements Lifecycle, InitializingBean {

	private volatile boolean running = false;

	private final ProcessBuilder processBuilder;

	private volatile Process process;

	private volatile InputStream stdout;

	private volatile OutputStream stdin;

	private boolean redirectErrorStream = true;

	private final Map<String, String> environment = new ConcurrentHashMap<>();

	private volatile String workingDirectory;

	private final static Log log = LogFactory.getLog(ShellCommand.class);

	private final String command;

	private final Object lifecycleLock = new Object();

	private int exitValue;

	private final boolean wait;

	/**
	 * Creates a process to invoke a shell command synchronously or asynchronously
	 *
	 * @param command he shell command with command line arguments as separate strings
	 * @param wait    set to true to wait for command to complete
	 */
	public ShellCommand(String command, boolean wait) {
		this.wait = wait;
		Assert.hasLength(command, "A shell command is required");
		this.command = command;
		ShellWordsParser shellWordsParser = new ShellWordsParser();
		List<String> commandPlusArgs = shellWordsParser.parse(command);
		Assert.notEmpty(commandPlusArgs, "The shell command is invalid: '" + command + "'");

		processBuilder = new ProcessBuilder(commandPlusArgs);
		processBuilder.redirectErrorStream(true);
	}

	/**
	 * Creates a process to invoke a shell command asynchronously
	 *
	 * @param command the shell command with command line arguments as separate strings
	 */
	public ShellCommand(String command) {
		this(command, false);
	}

	/**
	 * Start the process.
	 */
	@Override
	public void start() {
		synchronized (lifecycleLock) {
			if (!isRunning()) {
				if (log.isDebugEnabled()) {
					log.debug("starting process. Command = [" + command + "]");
				}

				try {
					process = processBuilder.start();

					stdout = process.getInputStream();
					stdin = process.getOutputStream();

					running = true;

					if (this.wait) {
						this.exitValue = waitForProcess(process);
					}

					if (log.isDebugEnabled()) {
						log.debug("process started. Command = [" + command + "]");
					}

					BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

					String line;
					while ((line = reader.readLine()) != null) {
						log.info("STDOUT " + line);
					}
				}
				catch (IOException e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(e.getMessage(), e);
				}

			}
		}
	}

	public int exitValue() {
		if (this.wait) {
			return this.exitValue;
		}
		throw new UnsupportedOperationException("'exitValue() is only valid for the synchronous option");
	}

	/**
	 * Stop the process and close streams.
	 */
	@Override
	public void stop() {
		synchronized (lifecycleLock) {
			if (isRunning()) {
				process.destroy();
				running = false;
			}
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	/**
	 * Set to true to redirect stderr to stdout.
	 *
	 * @param redirectErrorStream
	 */
	public void setRedirectErrorStream(boolean redirectErrorStream) {
		this.redirectErrorStream = redirectErrorStream;
	}

	/**
	 * A map containing environment variables to add to the process environment.
	 *
	 * @param environment
	 */
	public void setEnvironment(Map<String, String> environment) {
		this.environment.putAll(environment);
	}

	/**
	 * Set the process working directory
	 *
	 * @param workingDirectory the file path
	 */
	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		processBuilder.redirectErrorStream(redirectErrorStream);

		if (StringUtils.hasLength(workingDirectory)) {
			processBuilder.directory(new File(workingDirectory));
		}
		if (!CollectionUtils.isEmpty(environment)) {
			processBuilder.environment().putAll(environment);
		}
	}

	private int waitForProcess(Process process) {
		try {
			process.waitFor();
			return process.exitValue();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}