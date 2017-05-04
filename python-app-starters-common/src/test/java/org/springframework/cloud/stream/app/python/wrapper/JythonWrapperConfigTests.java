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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
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
public abstract class JythonWrapperConfigTests {

	@Autowired(required = false)
	JythonWrapper jythonWrapper;

	@Autowired(required = false)
	JGitResourceRepository repository;

	public static class TestWrapperConditionalOnWrapperScript extends JythonWrapperConfigTests {
		@Test
		public void test() {
			assertThat(jythonWrapper).isNull();
		}
	}

	@TestPropertySource(properties = { "wrapper.script=test_apps/upper.py",
			"git.uri=https://github.com/dturanski/python-apps" })
	public static class TestWrapperGitResource extends JythonWrapperConfigTests {
		@Ignore //TODO: Figure out how to test this case
		@Test
		public void test() throws IOException {
			assertThat(jythonWrapper.getScriptSource().getScriptAsString().trim())
					.isEqualTo("result = payload.upper()");
		}
	}

	@TestPropertySource(properties = { "wrapper.script=wrapper/simple_wrapper.py" })
	public static class TestWrapperClassPathResource extends JythonWrapperConfigTests {
		@Test
		public void test() throws IOException {
			assertThat(repository).isNull();

			System.out.println(jythonWrapper.getScriptSource().getScriptAsString());
			assertThat(jythonWrapper.getScriptSource().getScriptAsString().trim())
					.isEqualTo("result = payload.upper()");
		}
	}

	@TestPropertySource(properties = { "wrapper.script=file:src/test/resources/wrapper/simple_wrapper.py" })
	public static class TestWrapperFileResource extends JythonWrapperConfigTests {
		@Test
		public void test() throws IOException {
			assertThat(repository).isNull();

			System.out.println(jythonWrapper.getScriptSource().getScriptAsString());
			assertThat(jythonWrapper.getScriptSource().getScriptAsString().trim())
					.isEqualTo("result = payload.upper()");
		}
	}

	@TestPropertySource(properties = { "wrapper.script=wrapper/simple_wrapper.py", "use.shell=true" })
	public static class TestWrapperWithShellCommandProcessor extends JythonWrapperConfigTests {
		@Test
		public void test() throws IOException {
			assertThat(repository).isNull();
			assertThat(jythonWrapper).isInstanceOf(ShellCommandProcessorJythonWrapper.class);

		}
	}

	@SpringBootApplication
	@Import(JythonWrapperConfiguration.class)
	static class PythonApplication {

		@Configuration
		static class ShellConfiguration {
			@Bean
			@ConditionalOnProperty("use.shell")
			public ShellCommandProcessor shellCommandProcessor() {
				return new ShellCommandProcessor(new ByteArrayCrLfSerializer(), "python");
			}
		}

		@Configuration
		@Import({ ShellConfiguration.class, JythonWrapperConfiguration.class })
		static class TestJythonWrapperConfiguration {
		}

	}
}
