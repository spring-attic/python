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

package org.springframework.cloud.stream.app.python;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/


public class PythonAppDeployerTests {

	private PythonAppDeployer pythonAppDeployer = new ClassPathPythonAppDeployer();

	@Test
	public void testCopy() {
		pythonAppDeployer.deploy();
		assertThat(new File(pythonAppDeployer.getTargetDir()).isDirectory()).isTrue();
		assertThat(new File(pythonAppDeployer.getTargetDir()).exists()).isTrue();
		assertThat(new File(pythonAppDeployer.getTargetDir()).list()).isNotEmpty();
	}

	@After
	public void cleanUp() throws IOException {
		FileUtils.deleteDirectory(new File(pythonAppDeployer.getTargetDir()));
		assertThat(new File(pythonAppDeployer.getTargetDir()).exists()).isFalse();
	}
}