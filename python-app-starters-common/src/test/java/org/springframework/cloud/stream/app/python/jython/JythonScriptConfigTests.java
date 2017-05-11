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

package org.springframework.cloud.stream.app.python.jython;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.context.annotation.Import;
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
public abstract class JythonScriptConfigTests {

	@Autowired
	JythonScriptExecutor jythonScriptExecutor;

	@Autowired(required = false)
	JGitResourceRepository repository;

	@TestPropertySource(properties = { "jython.script=test-wrappers/upper.py",
			"git.uri=https://github.com/dturanski/python-apps" })
	public static class TestJythonGitResource extends JythonScriptConfigTests {
		@Ignore //TODO: Figure out how to test this case
		@Test
		public void test() throws IOException {
			assertThat(jythonScriptExecutor.getScriptSource().getScriptAsString().trim())
					.isEqualTo("result = payload.upper()");
		}
	}

	@TestPropertySource(properties = { "jython.script=wrapper/simple_wrapper.py" })
	public static class TestJythonClassPathResource extends JythonScriptConfigTests {
		@Test
		public void test() throws IOException {
			assertThat(repository).isNull();
			assertThat(jythonScriptExecutor.getScriptSource().getScriptAsString().trim())
					.isEqualTo("result = payload.upper()");
		}
	}

	@TestPropertySource(properties = { "jython.script=file:src/test/resources/wrapper/simple_wrapper.py" })
	public static class TestJythonFileResource extends JythonScriptConfigTests {
		@Test
		public void test() throws IOException {
			assertThat(repository).isNull();

			System.out.println(jythonScriptExecutor.getScriptSource().getScriptAsString());
			assertThat(jythonScriptExecutor.getScriptSource().getScriptAsString().trim())
					.isEqualTo("result = payload.upper()");
		}
	}


	@SpringBootApplication
	@Import(JythonScriptConfiguration.class)
	static class PythonApplication {
	}
}
