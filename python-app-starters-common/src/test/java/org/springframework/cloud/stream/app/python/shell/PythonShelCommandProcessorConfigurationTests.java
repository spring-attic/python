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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@DirtiesContext

public abstract class PythonShelCommandProcessorConfigurationTests {

	@Autowired
	ShellCommandProcessor processor;

	@Autowired
	PythonShellCommandProcessorProperties properties;

	@TestPropertySource(properties = { "python.script=echo.py", "python.basedir=." })
	public static class TestCWDpath extends PythonShelCommandProcessorConfigurationTests {
		@Test
		public void test() throws IOException {
			assertThat(processor).isNotNull();
			assertThat(properties.getPath()).isEmpty();
			assertThat(processor.getCommand()).isEqualTo("python echo.py");
		}
	}

	@TestPropertySource(properties = { "python.script=echo.py", "python.basedir=scripts/python" })
	public static class TestRelativePath extends PythonShelCommandProcessorConfigurationTests {
		@Test
		public void test() throws IOException {
			assertThat(processor).isNotNull();
			assertThat(properties.getPath()).isEqualTo("scripts/python");
			assertThat(processor.getCommand()).isEqualTo("python scripts/python/echo.py");

		}
	}

	@TestPropertySource(properties = { "python.script=echo.py", "python.basedir=/scripts/python" })
	public static class TestAbsolutePath extends PythonShelCommandProcessorConfigurationTests {
		@Test
		public void test() throws IOException {
			assertThat(processor).isNotNull();
			assertThat(properties.getPath()).isEqualTo("/scripts/python");
			assertThat(processor.getCommand()).isEqualTo("python /scripts/python/echo.py");
		}
	}

	@SpringBootApplication
	@Import(PythonShellCommandProcessorConfiguration.class)
	static class PythonApplication {

	}
}
