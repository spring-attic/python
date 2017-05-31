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
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.AfterClass;
import org.junit.Ignore;
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
	public void testCfScript() throws IOException {

		String command = PythonEnvironmentHelper.wrapCommandforCloud("python foo.py");
		File script = new File(PythonEnvironmentHelper.CF_SCRIPT_FILE_NAME);
		assertThat(command).isEqualTo(script.getAbsolutePath());

		assertThat(script.exists()).isTrue();
		assertThat(script.canExecute()).isTrue();
		String contents = FileUtils.readFileToString(script, StandardCharsets.UTF_8);
		assertThat(contents).contains("python foo.py");
		assertThat(contents).contains(PythonEnvironmentHelper.CF_PROFILE_PATH);
	}

	@Test
	@Ignore
	/*
	 * This will execute the install but won't install if pip is available.
	 */ public void testPipInstallScript() {
		String path = System.getProperty("user.home") + File.separator + ".local" + File.separator + "bin" + File
				.separator + "pip";
		doPipInstallTest("fooBarBaz", path);
	}

	@Test
	@Ignore
	public void testPipInstallScriptExistingPip() {
		doPipInstallTest("pip", "");
	}

	private void doPipInstallTest(String pipCommand, String expectedResult) {

		String path = System.getProperty("user.home") + File.separator + ".local" + File.separator + "bin";
		boolean previousLocalPip = new File(path + File.separator + "pip").exists();

		String installLocation = PythonEnvironmentHelper.installPipIfNecessary(pipCommand);
		assertThat(installLocation).isEqualTo(expectedResult);


		if (!previousLocalPip) {
			for (File file : FileUtils.listFiles(new File(path), new WildcardFileFilter("pip*"), null)) {
				System.out.println("deleting " + file.getAbsolutePath());
				file.delete();
			}

			String libpath = System.getProperty("user.home") + File.separator + ".local" +
					File.separator +"lib" + File.separator +"python2.7" + File.separator + "site-packages";
			for (File file : FileUtils
					.listFiles(new File(libpath), TrueFileFilter.INSTANCE, new WildcardFileFilter("pip*"))) {
				if (file.getParentFile().getName().startsWith("pip")) {
					System.out.println("deleting " + file.getAbsolutePath());
					file.delete();
				}
			}
		}

	}

	@AfterClass
	public static void cleanUp() {
		File cfScript = new File(PythonEnvironmentHelper.CF_SCRIPT_FILE_NAME);
		if (cfScript.exists()) {
			cfScript.delete();
		}
		File pipInstallScript = new File(PythonEnvironmentHelper.PIP_INSTALL_SCRIPT_FILE_NAME);
		if (pipInstallScript.exists()) {
			pipInstallScript.delete();
		}
	}
}
