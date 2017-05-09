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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.io.data.Page;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@DirtiesContext
public abstract class PythonProcessorTests {

	@Autowired
	Processor processor;

	@Autowired
	MessageCollector messageCollector;

	@Autowired
	ObjectMapper objectMapper;

	@TestPropertySource(properties = { "python.basedir=python", "python.script=processor_example.py" })
	public static class TestSimple extends PythonProcessorTests {
		@Test
		public void test() throws InterruptedException {
			Message<String> message = new GenericMessage<>("hello world");
			processor.input().send(message);
			Message<String> received = (Message<String>) messageCollector.forChannel(processor.output()).poll(1, TimeUnit.SECONDS);
			assertThat(received.getPayload()).isEqualTo("hello world");
		}
	}

	@TestPropertySource(properties = { "python.basedir=file:src/test/resources/python",
			"python.script=processor_example.py" })
	public static class TestSimpleFileSystem extends PythonProcessorTests {
		@Test
		public void test() throws InterruptedException {
			Message<String> message = new GenericMessage<>("hello world");
			processor.input().send(message);
			Message<String> received = (Message<String>) messageCollector.forChannel(processor.output()).poll(1,
					TimeUnit.SECONDS);
			assertThat(received.getPayload()).isEqualTo("hello world");
		}
	}

	@TestPropertySource(properties = { "python.basedir=python", "python.script=pickle_page_example.py",
			"python.encoder=BINARY", "wrapper.script=wrapper/page_wrapper.py" })
	public static class TestPicklePage extends PythonProcessorTests {
		@Test
		public void test() throws IOException, InterruptedException {
			Message<Page> message = new GenericMessage<>(new Page());
			processor.input().send(message);
			Message<String> received = (Message<String>) messageCollector.forChannel(processor.output()).poll(1,
					TimeUnit.SECONDS);

			Page page = objectMapper.readValue(received.getPayload(), Page.class);
			assertThat(page.getLinks()).containsKeys("google", "yahoo", "pivotal");
			assertThat(page.getLinks().get("pivotal").toString()).isEqualTo("http://www.pivotal.io");
		}
	}

	@TestPropertySource(properties = { "git.uri=https://github.com/dturanski/python-apps",
			"python.basedir=test-stream-scripts/time-transformer",
			"python.script=time-delta.py"
		})
	public static class TestGitRepo extends PythonProcessorTests {
		@Test
		@Ignore //TODO figure out a way to mock this
		public void test() throws InterruptedException {
			Message<String> message = new GenericMessage<>("06/01/16 09:45:11");
			processor.input().send(message);
			Message<String> received = (Message<String>) messageCollector.forChannel(processor.output()).poll(1,
					TimeUnit.SECONDS);
			System.out.println(received.getPayload());
		}
	}

	@SpringBootApplication
	@Import(PythonLocalProcessorConfiguration.class)
	public static class PythonApplication {

	}

}