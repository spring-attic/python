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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.cloud.stream.app.python.shell.PythonAppDeployer;
import org.springframework.cloud.stream.app.python.shell.PythonGitAppDeployerConfiguration;
import org.springframework.cloud.stream.app.python.shell.PythonShellCommandConfiguration;
import org.springframework.cloud.stream.app.python.shell.PythonShellCommandProperties;
import org.springframework.cloud.stream.app.python.shell.TcpProperties;
import org.springframework.cloud.stream.app.python.tcp.EncoderDecoderFactoryBean;
import org.springframework.cloud.stream.app.python.wrapper.JythonWrapperConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.shell.ShellCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.config.TcpConnectionFactoryFactoryBean;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpMessageMapper;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.messaging.MessageChannel;

/**
 * A Processor that forks a shell to run a Python app configured as processor, sending and receiving messages via
 * stdin/stdout. Optionally this may use a Jython wrapper script to transform data to and from the remote app. If no
 * wrapper is configured, the payload must be String.
 *
 * @author David Turanski
 **/

@EnableBinding(Processor.class)
@Import({ PythonShellCommandConfiguration.class, JythonWrapperConfiguration.class,
		PythonGitAppDeployerConfiguration.class })
@EnableConfigurationProperties({ TcpProperties.class, PythonShellCommandProperties.class })
public class PythonLocalProcessorConfiguration implements InitializingBean {

	@Autowired(required = false)
	private PythonAppDeployer pythonAppDeployer;

	@Autowired
	private ShellCommand shellCommand;

	@Autowired
	private Processor channels;

	@Autowired
	private TcpProperties properties;

	@Autowired
	private PythonShellCommandProperties scriptProperties;

	@Autowired(required = false)
	private JythonScriptExecutor jythonWrapper;

	@Bean
	public MessageChannel monitorInput() {
		return new DirectChannel();
	}

	@Bean
	public MessageChannel monitorOutput() {
		return new DirectChannel();
	}

	@Bean
	public TcpReceivingChannelAdapter monitorAdapter(
			@Qualifier("tcpMonitorConnectionFactory") AbstractConnectionFactory connectionFactory) {
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(connectionFactory);
		adapter.setClientMode(true);
		adapter.setRetryInterval(this.properties.getRetryInterval());
		adapter.setOutputChannel(monitorOutput());
		adapter.setAutoStartup(false);
		return adapter;
	}



	@Bean
	@ServiceActivator(inputChannel = Processor.INPUT)
	public TcpProcessor tcpProcessor(
			@Qualifier("tcpClientConnectionFactory") AbstractConnectionFactory connectionFactory) {
		TcpProcessor tcpProcessor = new TcpProcessor(properties.getCharset());
		tcpProcessor.setConnectionFactory((AbstractClientConnectionFactory) connectionFactory);
		tcpProcessor.setReplyChannel(channels.output());
		tcpProcessor.setContentType(scriptProperties.getContentType());
		return tcpProcessor;
	}

	@Bean
	@ServiceActivator(inputChannel = "monitorInput")
	public TcpSendingMessageHandler monitorMessageHandler(
			@Qualifier("tcpMonitorConnectionFactory") AbstractConnectionFactory connectionFactory) {
		TcpSendingMessageHandler sendingMessageHandler = new TcpSendingMessageHandler();
		sendingMessageHandler.setConnectionFactory(connectionFactory);
		return sendingMessageHandler;
	}

	@Bean
	public TcpConnectionFactoryFactoryBean tcpClientConnectionFactory(
			@Qualifier("tcpClientEncoder") AbstractByteArraySerializer encoder,
			@Qualifier("tcpClientMapper") TcpMessageMapper mapper,
			@Qualifier("tcpClientDecoder") AbstractByteArraySerializer decoder) throws Exception {
		return connectionFactoryFactoryBean(encoder, mapper, decoder, properties.getPort());

	}

	@Bean
	public TcpConnectionFactoryFactoryBean tcpMonitorConnectionFactory(
			@Qualifier("tcpClientEncoder") AbstractByteArraySerializer encoder,
			@Qualifier("tcpClientMapper") TcpMessageMapper mapper,
			@Qualifier("tcpClientDecoder") AbstractByteArraySerializer decoder) throws Exception {
		return connectionFactoryFactoryBean(encoder, mapper, decoder, properties.getMonitorPort());
	}

	private TcpConnectionFactoryFactoryBean connectionFactoryFactoryBean(AbstractByteArraySerializer encoder,
			TcpMessageMapper mapper, AbstractByteArraySerializer decoder, int port) throws Exception {
		TcpConnectionFactoryFactoryBean factoryBean = new TcpConnectionFactoryFactoryBean();
		factoryBean.setType("client");
		factoryBean.setUsingNio(this.properties.isNio());
		factoryBean.setUsingDirectBuffers(this.properties.isUseDirectBuffers());
		factoryBean.setSerializer(encoder);
		factoryBean.setDeserializer(decoder);
		factoryBean.setSoTimeout(this.properties.getSocketTimeout());
		factoryBean.setMapper(mapper);
		factoryBean.setSingleUse(Boolean.FALSE);
		factoryBean.setHost("localhost");
		factoryBean.setPort(port);
		return factoryBean;

	}

	@Bean
	public EncoderDecoderFactoryBean tcpClientEncoder() {
		return new EncoderDecoderFactoryBean(this.properties.getEncoder());
	}

	@Bean
	public TcpMessageMapper tcpClientMapper() {
		TcpMessageMapper mapper = new TcpMessageMapper();
		mapper.setCharset(this.properties.getCharset());
		return mapper;
	}

	@Bean
	public EncoderDecoderFactoryBean tcpClientDecoder() {
		EncoderDecoderFactoryBean factoryBean = new EncoderDecoderFactoryBean(this.properties.getDecoder());
		factoryBean.setMaxMessageSize(this.properties.getBufferSize());
		return factoryBean;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (pythonAppDeployer != null) {
			pythonAppDeployer.deploy();
		}
		shellCommand.executeAsync();
//		if (checkTcpConnection(properties.getMonitorPort())) {
//			//monitorAdapter.start();
//		}
//		else {
//			throw new RuntimeException("Unable to connect to shell process " + shellCommand.getCommand());
//		}
//		if (checkTcpConnection(properties.getPort())) {
//
//		}
//		else {
//			throw new RuntimeException("Unable to connect to shell process " + shellCommand.getCommand());
//		}

	}

//	private boolean checkTcpConnection(int port) {
//		int max_tries = 3;
//
//		TcpSocketFactorySupport socketFactorySupport = new DefaultTcpNetSocketFactorySupport();
//		int tries = 0;
//		while (tries++ < max_tries) {
//			try {
//				socketFactorySupport.getSocketFactory().createSocket("localhost", port);
//				return true;
//			}
//			catch (IOException e) {
//				try {
//					Thread.sleep(1000);
//				}
//				catch (InterruptedException e1) {
//					Thread.interrupted();
//				}
//			}
//		}
//		return false;
//	}

}