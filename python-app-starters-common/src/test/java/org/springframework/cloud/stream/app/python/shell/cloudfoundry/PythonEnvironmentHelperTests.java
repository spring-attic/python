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

package org.springframework.cloud.stream.app.python.shell.cloudfoundry;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/
public class PythonEnvironmentHelperTests {
	@Test
	public void test() throws IOException {
		PythonEnvironmentHelper helper = new PythonEnvironmentHelper();

		String command = helper.wrappedCommand("python foo.py");
		File script = new File(PythonEnvironmentHelper.SCRIPT_FILE_NAME);
		assertThat(command).isEqualTo(script.getAbsolutePath());

		assertThat(script.exists()).isTrue();
		assertThat(script.canExecute()).isTrue();
		String contents = FileUtils.readFileToString(script, StandardCharsets.UTF_8);
		assertThat(contents).contains("python foo.py");
		assertThat(contents).contains(PythonEnvironmentHelper.PROFILE_PATH);

	}

	@AfterClass
	public static void cleanUp() {
		new File(PythonEnvironmentHelper.SCRIPT_FILE_NAME).delete();
	}
}
