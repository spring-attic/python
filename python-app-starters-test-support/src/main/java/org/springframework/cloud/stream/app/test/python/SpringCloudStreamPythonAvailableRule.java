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

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author David Turanski
 **/
public class SpringCloudStreamPythonAvailableRule extends PythonAvailableRule {
	public static final String SPRINGCLOUDSTREAM_PYTHON_MODULE = "springcloudstream";

	public SpringCloudStreamPythonAvailableRule() {
		this(null);
	}


	public SpringCloudStreamPythonAvailableRule(String version) {

		super("pip", "install", SPRINGCLOUDSTREAM_PYTHON_MODULE +
				(StringUtils.hasText(version)? ("=="+version):""));

		processBuilder.redirectErrorStream(true);
	}

	@Override
	protected void cleanupResource() throws Exception {

	}

	@Override
	protected void obtainResource() throws Exception {
		Process process = null;
		try {

			process = processBuilder.start();

			InputStream stdout = process.getInputStream();

			int exitValue = process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println("STDOUT " + line);
			}

			if (exitValue != 0) {
				throw new Exception("command failed with exit value " + exitValue);
			}
		}
		finally {
			if (process != null) {
				process.destroy();
			}
		}
	}
}