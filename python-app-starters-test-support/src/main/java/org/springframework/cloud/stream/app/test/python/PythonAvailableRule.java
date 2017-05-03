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

package org.springframework.cloud.stream.app.test.python;

import org.springframework.cloud.stream.test.junit.AbstractExternalResourceTestSupport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author David Turanski
 */
public class PythonAvailableRule extends AbstractExternalResourceTestSupport<Object> {
	protected final ProcessBuilder processBuilder;

	public PythonAvailableRule() {
		super("python command");
		processBuilder = new ProcessBuilder("python");
	}

	protected PythonAvailableRule(String... command) {
		super("python command " + command[0]);
		processBuilder = new ProcessBuilder(command);
	}

	@Override
	protected void cleanupResource() throws Exception {

	}

	@Override
	protected void obtainResource() throws Exception {
		Process process = null;
		try {
			process = processBuilder.start();
		}
		finally {
			if (process != null) {
				process.destroy();
			}
		}
	}
}