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

package org.springframework.cloud.stream.app.python.local.wrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.spring.io.data.Page;
import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.cloud.stream.app.python.local.processor.TcpProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.support.GenericMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author David Turanski
 **/
public class JythonScriptExecutorTests {

	private TcpProcessor tcpProcessor = mock(TcpProcessor.class);

	@Before
	public void setUp() {
		when(tcpProcessor.sendAndReceive(any())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (String) args[0];
			}
		});
	}

	@Test
	public void simple() throws Exception {
		JythonScriptExecutor jythonScriptExecutor = new JythonScriptExecutor(
				new ClassPathResource("wrapper/simple_wrapper.py"));
		jythonScriptExecutor.afterPropertiesSet();
		assertThat(jythonScriptExecutor.execute(new GenericMessage<String>("hello"))).isEqualTo("HELLO");
	}

	@Test
	public void picklePage() throws Exception {
		TcpProcessorJythonWrapper jythonWrapper = new TcpProcessorJythonWrapper(
				new ClassPathResource("wrapper/page_wrapper.py"), tcpProcessor);
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
		JythonScriptExecutor jythonScriptExecutor = new JythonScriptExecutor(
				new ClassPathResource("wrapper/simple_wrapper.py"));
		jythonScriptExecutor.afterPropertiesSet();
		Object result = jythonScriptExecutor.execute(new GenericMessage<String>("hello"));
		assertThat(result).isEqualTo("HELLO");

	}
}
