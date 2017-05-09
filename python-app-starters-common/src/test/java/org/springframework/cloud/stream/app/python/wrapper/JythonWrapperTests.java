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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.spring.io.data.Page;
import org.springframework.cloud.stream.app.python.wrapper.JythonWrapper;
import org.springframework.cloud.stream.app.python.wrapper.ShellCommandProcessorJythonWrapper;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.support.GenericMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author David Turanski
 **/
public class JythonWrapperTests {

	ShellCommandProcessor shellCommandProcessor = mock(ShellCommandProcessor.class);

	@Before
	public void setUp() {
		when(shellCommandProcessor.sendAndReceive(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (String) args[0];
			}
		});
	}

	@Test
	public void simple() {
		JythonWrapper jythonWrapper = new JythonWrapper(new ClassPathResource("wrapper/simple_wrapper.py"));
		assertThat(jythonWrapper.execute(new GenericMessage<String>("hello"))).isEqualTo("HELLO");
	}

	@Test
	public void picklePage() throws Exception {
		ShellCommandProcessorJythonWrapper jythonWrapper = new ShellCommandProcessorJythonWrapper(
				new ClassPathResource("wrapper/page_wrapper.py"), shellCommandProcessor);
		jythonWrapper.afterPropertiesSet();
		Object result = jythonWrapper.execute(new GenericMessage<Page>(new Page()));
		assertThat(result).isNotNull();
		Page page = new ObjectMapper().readValue(result.toString(), Page.class);
	}

	@Test
	@Ignore
	public void scriptSource() throws Exception {
		Resource script = new UrlResource(
				"https://github.com/dturanski/python-apps/blob/master/test-wrappers/upper.py");
		JythonWrapper jythonWrapper = new JythonWrapper(new ClassPathResource("wrapper/simple_wrapper.py"));
		Object result = jythonWrapper.execute(new GenericMessage<String>("hello"));
		assertThat(result).isEqualTo("HELLO");

	}
}
