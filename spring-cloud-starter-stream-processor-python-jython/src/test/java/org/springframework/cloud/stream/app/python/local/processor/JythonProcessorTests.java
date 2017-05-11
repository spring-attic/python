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

package org.springframework.cloud.stream.app.python.local.processor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

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

	@TestPropertySource(properties = { "jython.script=wrapper/simple_wrapper.py" })
	public static class TestSimple extends JythonProcessorTests {
		@Test
		public void test() throws InterruptedException {
			Message<String> message = new GenericMessage<>("hello world");
			processor.input().send(message);
			Message<String> received = (Message<String>) messageCollector.forChannel(processor.output())
					.poll(1, TimeUnit.SECONDS);
			assertThat(received.getPayload()).isEqualTo("HELLO WORLD");
		}
	}

	@TestPropertySource(properties = { "jython.script=file:./src/test/resources/wrapper/simple_wrapper.py" })
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

	@TestPropertySource(properties = { "jython.script=test-wrappers/upper.py",
			"git.uri=https://github.com/dturanski/python-apps" })
	@Ignore
	public static class TestGit extends JythonProcessorTests {
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
	@Import(JythonProcessorConfiguration.class)
	public static class PythonApplication {

	}

}