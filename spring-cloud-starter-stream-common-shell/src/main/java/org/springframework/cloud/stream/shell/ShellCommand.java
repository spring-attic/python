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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates a process to execute a shell command synchronously.
 *
 * @author David Turanski
 * @author Gary Russell
 */
public class ShellCommand implements DisposableBean {

	private volatile boolean running = false;

	private final ProcessBuilder processBuilder;

	private volatile Process process;

	private final Map<String, String> environment = new ConcurrentHashMap<>();

	private volatile String workingDirectory;

	private final static Log log = LogFactory.getLog(ShellCommand.class);

	private final String command;

	private final Object lifecycleLock = new Object();

	/**
	 * Creates a process to invoke a shell command synchronously or asynchronously
	 *
	 * @param command he shell command with command line arguments as separate strings
	 */
	public ShellCommand(String command) {

		Assert.hasLength(command, "A shell command is required");
		this.command = command;
		ShellWordsParser shellWordsParser = new ShellWordsParser();
		List<String> commandPlusArgs = shellWordsParser.parse(command);
		Assert.notEmpty(commandPlusArgs, "The shell command is invalid: '" + command + "'");

		processBuilder = new ProcessBuilder(commandPlusArgs);
		processBuilder.redirectErrorStream(true);
	}

	@Override
	public void destroy() throws Exception {
		this.process.destroy();
	}

	public static class CommandResponse {
		private final int exitValue;
		private final String output;

		CommandResponse(int exitValue, String output) {
			this.exitValue = exitValue;
			this.output = output;
		}

		public int exitValue() {
			return exitValue;
		}

		public String output() {
			return output;
		}
	}

	public void executeAsync() {
		this.init();
		synchronized (lifecycleLock) {
			if (!isRunning()) {
				if (log.isInfoEnabled()) {
					log.info("starting process. Command = [" + command + "]");
				}

			}
			try {
				process = processBuilder.start();

				running = true;

				monitorProcess();

				if (log.isInfoEnabled()) {
					log.info("process started. Command = [" + command + "]");
				}
			}
			catch (IOException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

	/**
	 * Execute the process
	 */
	public CommandResponse execute() {
		CommandResponse response = new CommandResponse(0, "");
		this.init();
		synchronized (lifecycleLock) {
			if (!isRunning()) {
				if (log.isDebugEnabled()) {
					log.debug("starting process. Command = [" + command + "]");
				}

				try {
					process = processBuilder.start();

					running = true;

					if (log.isDebugEnabled()) {
						log.debug("process started. Command = [" + command + "]");
					}

					int exitValue = waitForProcess(process);

					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

					StringBuilder output = new StringBuilder("");
					String line;
					while ((line = reader.readLine()) != null) {
						output.append(line).append("\n");
					}

					response = new CommandResponse(exitValue, output.toString());

					this.stop();
				}
				catch (IOException e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(e.getMessage(), e);
				}

			}
		}

		return response;
	}

	/**
	 *
	 * @return the command.
	 */
	public String getCommand() {
		return this.command;
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

	private void init() {
		if (StringUtils.hasLength(workingDirectory)) {
			processBuilder.directory(new File(workingDirectory));
		}
		if (!CollectionUtils.isEmpty(environment)) {
			processBuilder.environment().putAll(environment);
		}
	}

	private boolean isRunning() {
		return running;
	}

	/**
	 * Runs a thread that waits for the Process result.
	 */
	private void monitorProcess() {
		new SimpleAsyncTaskExecutor().execute(new Runnable() {

			@Override
			public void run() {
				Process process = ShellCommand.this.process;
				if (process == null) {
					if (log.isDebugEnabled()) {
						log.debug("Process destroyed before starting process monitor");
					}
					return;
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				StringBuilder output = new StringBuilder("");
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
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

	/**
	 * Stop the process and close streams.
	 */

	private void stop() {
		synchronized (lifecycleLock) {
			if (isRunning()) {
				process.destroy();
				running = false;
			}
		}
	}

}