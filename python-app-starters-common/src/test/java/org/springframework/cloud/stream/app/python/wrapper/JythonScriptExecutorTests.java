/*
 * Copyright 2017 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.springframework.cloud.stream.app.python.wrapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.support.GenericMessage;

/**
 * @author David Turanski
 **/
public class JythonScriptExecutorTests {

	@Test
	public void simple() throws Exception {
		JythonScriptExecutor jythonScriptExecutor = new JythonScriptExecutor(
			new ClassPathResource("wrapper/simple_wrapper.py"));
		jythonScriptExecutor.afterPropertiesSet();
		assertThat(jythonScriptExecutor.execute(new GenericMessage<String>("hello"))).isEqualTo("HELLO");
	}

	@Test
	@Ignore
	public void scriptSource() throws Exception {
		Resource script = new UrlResource(
			"https://github.com/dturanski/python-apps/blob/master/test-wrappers/upper.py");
		JythonScriptExecutor jythonScriptExecutor = new JythonScriptExecutor(
			new ClassPathResource("wrapper/simple_wrapper.py"));
		jythonScriptExecutor.afterPropertiesSet();
		Object result = jythonScriptExecutor.execute(new GenericMessage<String>("hello"));
		assertThat(result).isEqualTo("HELLO");

	}
}
