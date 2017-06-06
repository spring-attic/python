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
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.cloud.stream.app.python.shell.PythonAppDeployer;
import org.springframework.cloud.stream.app.python.shell.PythonGitAppDeployerConfiguration;
import org.springframework.cloud.stream.app.python.shell.PythonShellCommandProcessorConfiguration;
import org.springframework.cloud.stream.app.python.shell.PythonShellCommandProcessorProperties;
import org.springframework.cloud.stream.app.python.wrapper.JythonWrapperConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MutableMessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * A Processor that forks a shell to run a Python app configured as processor, sending and receiving messages via
 * stdin/stdout. Optionally this may use a Jython wrapper script to transform data to and from the remote app. If no
 * wrapper is configured, the payload must be String.
 *
 * @author David Turanski
 **/
@EnableBinding(Processor.class)
@Import({ PythonShellCommandProcessorConfiguration.class, JythonWrapperConfiguration.class,
		PythonGitAppDeployerConfiguration.class})
public class PythonLocalProcessorConfiguration implements InitializingBean {

	@Autowired(required = false)
	private PythonAppDeployer pythonAppDeployer;

	@Autowired
	private ShellCommandProcessor shellCommandProcessor;

	@Autowired
	private PythonShellCommandProcessorProperties properties;

	@Autowired(required = false)
	private JythonScriptExecutor jythonWrapper;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pythonAppDeployer != null) {
			pythonAppDeployer.deploy();
		}
		shellCommandProcessor.start();
	}

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Message<Object> process(Message<?> message) {

		Object result = null;

		if (jythonWrapper != null) {
			result = jythonWrapper.execute(message);
		}
		else {
			if (message.getPayload() instanceof String) {
				result = shellCommandProcessor.sendAndReceive((String) message.getPayload());
			}
			else if (message.getPayload() instanceof byte[]) {
				result = shellCommandProcessor.sendAndReceive((byte[]) message.getPayload());
			}
			else {
				result = shellCommandProcessor.sendAndReceive((String) message.getPayload().toString());
			}
		}

		if (properties.getContentType() != null) {
			return MutableMessageBuilder.withPayload(result).copyHeaders(message.getHeaders())
					.setHeader(MessageHeaders.CONTENT_TYPE, properties.getContentType()).build();
		}

		else {
			return MutableMessageBuilder.withPayload(result).copyHeaders(message.getHeaders()).build();
		}
	}
}