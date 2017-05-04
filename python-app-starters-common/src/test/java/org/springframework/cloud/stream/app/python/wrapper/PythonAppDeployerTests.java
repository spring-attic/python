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

package org.springframework.cloud.stream.app.python.wrapper;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.cloud.stream.app.python.shell.ClassPathPythonAppDeployer;
import org.springframework.cloud.stream.app.python.shell.FileSystemPythonAppDeployer;
import org.springframework.cloud.stream.app.python.shell.PythonAppDeployer;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/


public class PythonAppDeployerTests {



	@Test
	public void testClassPathDeployer() throws IOException {
		PythonAppDeployer pythonAppDeployer = new ClassPathPythonAppDeployer();
		pythonAppDeployer.deploy();
		assertThat(pythonAppDeployer.getAppDir().getFile().isDirectory()).isTrue();
		assertThat(pythonAppDeployer.getAppDir().exists()).isTrue();
		assertThat(pythonAppDeployer.getAppDir().getFile().list()).isNotEmpty();
	}

	@Test
	public void testFileSystemDeployer() throws IOException {
		FileSystemPythonAppDeployer pythonAppDeployer = new FileSystemPythonAppDeployer();
		pythonAppDeployer.setAppDir(new FileSystemResource("src/test/resources/my-python-app"));
		System.out.println(pythonAppDeployer.getAppDirPath());
		pythonAppDeployer.deploy();
		assertThat(pythonAppDeployer.getAppDir().getFile().isDirectory()).isTrue();
		assertThat(pythonAppDeployer.getAppDir().exists()).isTrue();
		assertThat(pythonAppDeployer.getAppDir().getFile().list()).isNotEmpty();
	}

}