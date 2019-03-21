/*
 * Copyright 2017-2018 the original author or authors.
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

package org.springframework.cloud.stream.app.python.http.processor;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author David Turanski
 * @author Chris Schaefer
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PythonHttpProcessorTests.PythonProcessorApp.class,
	webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
	properties = {
		"httpclient.urlExpression='http://localhost:' + @environment.getProperty('server.port') +'/py'",
		"httpclient.httpMethod=POST", "wrapper.script=src/test/resources/simple-test.py" })
@DirtiesContext
public class PythonHttpProcessorTests {

	/*
	 * WebEnvironment.RANDOM_PORT doesn't work here because the port value is added to the parent environment after the
	 * child contexts are created.
	 */
	@BeforeClass
	public static void setUp() {
		System.setProperty("server.port", String.valueOf(SocketUtils.findAvailableTcpPort()));
	}

	@AfterClass
	public static void tearDown() {
		System.clearProperty("server.port");
	}

	@Autowired
	private MessageCollector messageCollector;

	@Autowired
	private Processor processor;

	@Test
	public void testFlow() throws InterruptedException {

		processor.input().send(MessageBuilder.withPayload("Hello").build());
		Message<?> receivedMessage = messageCollector.forChannel(processor.output()).poll(1, TimeUnit.SECONDS);
		assertThat(receivedMessage).isNotNull();
		assertThat(receivedMessage.getPayload()).isEqualTo("PreHelloHttpPost");
	}

	@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
	@Import({ PythonHttpProcessorConfiguration.class})
	@RestController
	static class PythonProcessorApp {
		@PostMapping("/py")
		public String greet(@RequestBody String payload) {
			return payload + "Http";
		}

	}
}
