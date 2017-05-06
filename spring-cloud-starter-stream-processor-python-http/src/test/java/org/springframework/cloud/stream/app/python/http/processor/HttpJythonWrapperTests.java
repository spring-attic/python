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

package org.springframework.cloud.stream.app.python.http.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.aggregate.AggregateApplication;
import org.springframework.cloud.stream.aggregate.AggregateApplicationBuilder;
import org.springframework.cloud.stream.app.httpclient.processor.HttpclientProcessorConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author David Turanski
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=9000")
@DirtiesContext
public abstract class HttpJythonWrapperTests {

	@Autowired
	MessageCollector messageCollector;

	@Autowired
	AggregateApplication aggregateApplication;

	@TestPropertySource(properties = { "httpclient.urlExpression='http://localhost:9000/py'",
			"wrapper.script=simple-test.py", "httpclient.httpMethod=POST" })
	public static class SimpleWrapperTest extends HttpJythonWrapperTests {
		@Test
		public void testAggregateApplication() throws InterruptedException {
			Processor inProcessor = aggregateApplication.getBinding(Processor.class, "in");
			Processor outProcessor = aggregateApplication.getBinding(Processor.class, "out");
			inProcessor.input().send(MessageBuilder.withPayload("Hello").build());
			Message<?> receivedMessage = messageCollector.forChannel(outProcessor.output()).poll(1, TimeUnit.SECONDS);
			assertThat(receivedMessage).isNotNull();
			assertThat(receivedMessage.getPayload()).isEqualTo("PreHelloHttpPost");
		}
	}

	@SpringBootApplication
	@EnableWebSecurity
//	@Import(HttpclientProcessorConfiguration.class)
	static class PythonProcessorApp {
		@Bean
		public AggregateApplication pythonProcessorApp() {
			return new AggregateApplicationBuilder()
					.from(HttpJythonProcessorInputConfiguration.class)
					.namespace("in").via(HttpclientProcessorConfiguration.class)
					.to(HttpJythonProcessorOutputConfiguration.class).namespace("out").build();
		}

		@RestController
		public static class AdditionalController {

			@PostMapping("/py")
			public String greet(@RequestBody String payload) {
				return payload + "Http";
			}
		}

	}
}
