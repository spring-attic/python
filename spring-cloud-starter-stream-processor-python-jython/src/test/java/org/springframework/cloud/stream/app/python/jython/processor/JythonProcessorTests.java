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

package org.springframework.cloud.stream.app.python.jython.processor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author David Turanski
 **/

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@DirtiesContext
public abstract class JythonProcessorTests {

	@Autowired
	Processor processor;

	@Autowired
	MessageCollector messageCollector;

	@TestPropertySource(properties = { "jython.script=./src/test/resources/wrapper/simple_wrapper.py" })
	public static class TestSimpleFile extends JythonProcessorTests {

		@Test
		public void test() throws InterruptedException {
			Message<String> message = new GenericMessage<>("hello world");
			processor.input().send(message);
			Message<String> received = (Message<String>) messageCollector.forChannel(processor.output())
				.poll(1, TimeUnit.SECONDS);
			assertThat(received.getPayload()).isEqualTo("HELLO WORLD");
		}
	}

	@TestPropertySource(properties = { "jython.script=src/test/resources/wrapper/map_sentiments.py",
		"jython.variables=positive=0.6,neutral=0.4", "jython.delimiter=COMMA" })
	public static class TestWithVariables extends JythonProcessorTests {

		@Test
		public void test() throws InterruptedException {
			Message<Double> message = new GenericMessage<>(0.394);
			processor.input().send(message);
			Message<String> received = (Message<String>) messageCollector.forChannel(processor.output())
				.poll(1, TimeUnit.SECONDS);
			assertThat(received.getPayload()).isEqualTo("{\"sentiment\": \"Negative\"}");
		}
	}

	@TestPropertySource(properties = { "jython.script=test-wrappers/upper.py", "git.uri=https://example.com" })
	@ActiveProfiles("test")
	public static class TestGit extends JythonProcessorTests {

		@Autowired
		JGitResourceRepository resourceRepository;

		@Test
		public void test() throws InterruptedException {
			Message<String> message = new GenericMessage<>("hello world");
			processor.input().send(message);
			Message<String> received = (Message<String>) messageCollector.forChannel(processor.output())
				.poll(1, TimeUnit.SECONDS);
			assertThat(received.getPayload()).isEqualTo("HELLO WORLD");
		}
	}

	@SpringBootApplication
	@Import(PythonJythonProcessorConfiguration.class)
	public static class PythonApplication {

		@Bean
		public BeanPostProcessor beanPostProcessor() {
			return new BeanPostProcessor() {
				@Override
				public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
					if (bean instanceof JGitResourceRepository) {
						return mockGitResourceRepository();
					}
					return bean;
				}
			};
		}

		private JGitResourceRepository mockGitResourceRepository() {
			JGitResourceRepository resourceRepository = mock(JGitResourceRepository.class);
			when(resourceRepository.getBasedir()).thenReturn(new File("src/test/resources"));
			return resourceRepository;
		}

		;
	}

}
