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

package org.springframework.cloud.stream.app.python.local.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.app.python.local.tcp.Exchanger;
import org.springframework.cloud.stream.app.python.local.tcp.TcpConnectionFactoryConfiguration;
import org.springframework.cloud.stream.app.python.local.tcp.TcpProcessor;
import org.springframework.cloud.stream.app.python.shell.TcpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory;
import org.springframework.messaging.MessageChannel;

/**
 * @author David Turanski
 **/
@Configuration
@Import(TcpConnectionFactoryConfiguration.class)
@EnableConfigurationProperties(TcpProperties.class)
public class TcpMonitorConfiguration {

	@Autowired
	private TcpProperties properties;

	@Bean
	public MessageChannel monitorInput() {
		return new DirectChannel();
	}

	@Bean
	public MessageChannel monitorOutput() {
		return new DirectChannel();
	}

	@Bean
	PythonProcessHealthIndicator pythonProcessHealthIndicator(Exchanger tcpMonitor){
		return new PythonProcessHealthIndicator(tcpMonitor);
	}

	@Bean
	@ServiceActivator(inputChannel = "monitorInput")
	public Exchanger tcpMonitor(
			@Qualifier("tcpMonitorConnectionFactory") AbstractConnectionFactory connectionFactory) {
		TcpProcessor tcpProcessor = new TcpProcessor(properties.getCharset());
		tcpProcessor.setConnectionFactory((AbstractClientConnectionFactory) connectionFactory);
		tcpProcessor.setReplyChannel(monitorOutput());
		tcpProcessor.start();
		return tcpProcessor;
	}

}
