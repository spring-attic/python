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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.cloud.stream.app.python.local.monitor.TcpMonitorConfiguration;
import org.springframework.cloud.stream.app.python.local.tcp.TcpProcessor;
import org.springframework.cloud.stream.app.python.local.tcp.TcpProcessorConfiguration;
import org.springframework.cloud.stream.app.python.local.wrapper.JythonWrapperConfiguration;
import org.springframework.cloud.stream.app.python.shell.PythonAppDeployer;
import org.springframework.cloud.stream.app.python.shell.PythonGitAppDeployerConfiguration;
import org.springframework.cloud.stream.app.python.shell.PythonShellCommandConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.shell.ShellCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.messaging.Message;

/**
 * A Processor that forks a shell to run a Python app configured as processor, sending and receiving messages via
 * tcp sockets. Optionally this may use a Jython wrapper script to transform data to and from the remote app.
 *
 * @author David Turanski
 **/

@EnableBinding(Processor.class)
@Import({ PythonShellCommandConfiguration.class, TcpProcessorConfiguration.class, TcpMonitorConfiguration.class,
		PythonGitAppDeployerConfiguration.class, JythonWrapperConfiguration.class })
public class PythonLocalProcessorConfiguration implements InitializingBean {

	@Autowired(required = false)
	private PythonAppDeployer pythonAppDeployer;

	@Autowired
	private ShellCommand shellCommand;

	@ConditionalOnBean(JythonScriptExecutor.class)
	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	@Bean
	public MessageProcessor<Object> messageProcessor(final JythonScriptExecutor jythonWrapper) {
		return new MessageProcessor<Object>() {
			@Override
			public Object processMessage(Message<?> message) {
				return jythonWrapper.execute(message);
			}
		};
	}

	@ConditionalOnMissingBean(JythonScriptExecutor.class)
	@ServiceActivator(inputChannel = Processor.INPUT)
	@Bean
	TcpProcessor tcpProcessorServiceActivator(TcpProcessor tcpProcessor) {
		return tcpProcessor;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pythonAppDeployer != null) {
			pythonAppDeployer.deploy();
		}
		shellCommand.executeAsync();
	}
}