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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

	@TestPropertySource(properties = { "python.script=echo.py",
			"python.basedir=scripts/python",
			"python.contentType=application/json"})
	public static class TestContentType extends PythonShelCommandProcessorConfigurationTests {
		@Test
		public void test() throws IOException {
			assertThat(processor).isNotNull();
			assertThat(properties.getPath()).isEqualTo("scripts/python");
			assertThat(processor.getCommand()).isEqualTo("python scripts/python/echo.py");
			assertThat(properties.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

		}
	}


	@TestPropertySource(properties = { "python.script=echo.py", "python.basedir=/scripts/python",
			"python.args=--categories=\"{'ecstatic':0.90,'happy':0.75,'warm':0.65,'meh':0.55,'cool':0.45,'gloomy':0.25,"
					+ "'doomed':0.0}\"" })
	public static class TestCommandArgs extends PythonShelCommandProcessorConfigurationTests {
		@Test
		public void test() throws IOException {
			assertThat(processor).isNotNull();
			System.out.println(processor.getCommand());
			assertThat(processor.getCommand()).isEqualTo(
					"python /scripts/python/echo.py --categories=\"{'ecstatic':0.90,'happy':0.75,'warm':0.65,"
							+ "'meh':0.55,'cool':0.45,'gloomy':0.25,'doomed':0.0}\"");
		}
	}

	@TestPropertySource(properties = { "python.script=echo.py", "python.basedir=/scripts/python",
			"python.args=--categories=\"{'ecstatic':0.90,'happy':0.75,'warm':0.65,'meh':0.55,'cool':0.45,'gloomy':0.25,"
					+ "'doomed':0.0}\"", "spring.profiles.active=cloud" })
	public static class TestCommandArgsForCloud extends PythonShelCommandProcessorConfigurationTests {

		private File pythonProcessor = new File("python_processor.sh");

		@Test
		public void test() throws IOException {
			assertThat(processor).isNotNull();
			assertThat(pythonProcessor.exists()).isTrue();
			assertThat(processor.getCommand()).isEqualTo(pythonProcessor.getAbsolutePath());
			List<String> lines = FileUtils.readLines(pythonProcessor, StandardCharsets.UTF_8);

			assertThat(lines).containsOnlyOnce(
					"python /scripts/python/echo.py --categories=\"{'ecstatic':0.90,'happy':0.75,'warm':0.65,"
							+ "'meh':0.55,'cool':0.45,'gloomy':0.25," + "'doomed':0.0}\"");
		}

		@After
		public void cleanUp() {
			if (pythonProcessor.exists()) {
				pythonProcessor.delete();
			}
		}
	}

	@SpringBootApplication
	@Import(PythonShellCommandProcessorConfiguration.class)
	static class PythonApplication {
		@Bean
		@Profile("cloud")
		public CloudFactory cloudFactory() {
			CloudFactory cloudFactory = mock(CloudFactory.class);
			when(cloudFactory.getCloud()).thenReturn(cloud());
			return cloudFactory;
		}

		@Bean
		@Profile("cloud")
		public Cloud cloud() {
			return mock(Cloud.class);
		}
	}
}
