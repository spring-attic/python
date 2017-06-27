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

package org.springframework.cloud.stream.app.python.local.tcp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.python.local.processor.PythonLocalProcessorConfiguration;
import org.springframework.cloud.stream.app.python.shell.TcpProperties;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Import;
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@DirtiesContext
public abstract class TcpClientConfigurationTests {

	@Autowired
	MessageCollector messageCollector;


	@TestPropertySource(properties = {
			"python.basedir=src/test/resources/python",
			"python.script=processor_example.py" })
	public static class DefaultClientConfigurationTest extends TcpClientConfigurationTests {
		@Autowired
		private TcpProperties properties;

		@Autowired
		@Qualifier("tcpClientConnectionFactory")
		private AbstractConnectionFactory clientConnectionFactory;

		@Autowired
		@Qualifier("tcpMonitorConnectionFactory")
		private AbstractConnectionFactory monitorConnectionFactory;

		@Test
		public void test() {
			assertThat(clientConnectionFactory.getPort()).isEqualTo(properties.getPort());
			assertThat(monitorConnectionFactory.getPort()).isEqualTo(properties.getMonitorPort());
		}
	}

	@SpringBootApplication
	@Import(TcpConnectionFactoryConfiguration.class)
	public static class PythonApplication {

	}
}
