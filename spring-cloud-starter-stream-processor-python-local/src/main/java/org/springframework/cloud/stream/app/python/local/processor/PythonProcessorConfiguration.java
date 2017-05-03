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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.python.ClassPathPythonAppDeployer;
import org.springframework.cloud.stream.app.python.JythonWrapper;
import org.springframework.cloud.stream.app.python.PythonAppDeployer;

import org.springframework.cloud.stream.app.python.properties.PythonWrapperProperties;

import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArraySingleTerminatorSerializer;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import java.io.File;
import java.util.Map;

/**
 * A Processor that forks a Python process, sending and receiving messages via TCP.
 *
 * @author David Turanski
 **/
@EnableBinding(Processor.class)
@EnableConfigurationProperties({ PythonProcessorProperties.class, PythonWrapperProperties.class })
public class PythonProcessorConfiguration implements InitializingBean {

	@Autowired
	private PythonProcessorProperties properties;

	@Autowired
	private PythonWrapperProperties wrapperProperties;

	@Autowired
	private PythonAppDeployer pythonAppDeployer;

	@Autowired
	private ShellCommandProcessor shellCommandProcessor;

	@Autowired(required = false)
	private JythonWrapper jythonWrapper;

	@Bean
	public AbstractByteArraySerializer serializer() {
		final byte BINARY_ENCODER_X1A = (byte) 26;
		AbstractByteArraySerializer serializer = null;
		switch (properties.getEncoder()) {
		case LF:
			serializer = new ByteArrayLfSerializer();
			break;
		case CRLF:
			serializer = new ByteArrayCrLfSerializer();
			break;
		case BINARY:
			serializer = new ByteArraySingleTerminatorSerializer(BINARY_ENCODER_X1A);
			break;
		}
		return serializer;
	}

	@Bean
	public ShellCommandProcessor shellCommandProcessor(AbstractByteArraySerializer serializer,
			PythonAppDeployer pythonAppDeployer) {
		Resource script = properties.getScriptResource();
		String filePath = StringUtils
				.join(new String[] { pythonAppDeployer.getTargetDir(), script.getFilename() }, File.separator);
		String command = StringUtils
				.join(new String[] { properties.getCommandName(), properties.getArgs(), filePath }, " ");

		ShellCommandProcessor shellCommandProcessor = new ShellCommandProcessor(serializer(), command);
		shellCommandProcessor.setAutoStart(false);
		return shellCommandProcessor;
	}

	@Bean
	public PythonAppDeployer pythonAppDeployer() {
		ClassPathPythonAppDeployer pythonAppDeployer = new ClassPathPythonAppDeployer();
		pythonAppDeployer.setSourceDir(properties.getRootPath().getFilename());
		pythonAppDeployer.setPipCommandName(properties.getPipCommandName());
		return pythonAppDeployer;
	}

	@Bean
	public JythonWrapper jythonWrapper(ShellCommandProcessor shellCommandProcessor) {
		return (wrapperProperties.getScript() == null) ?
				null :
				new JythonWrapper(wrapperProperties.getScriptResource(), shellCommandProcessor);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		pythonAppDeployer.deploy();
		shellCommandProcessor.start();
	}

	//TODO Support other payload types beside String
	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object process(@Headers Map<String, Object> headers, @Payload Object payload) {
		if (jythonWrapper != null) {
			return jythonWrapper.sendAndRecieve(payload);
		}
		else {
			if (payload instanceof String) {
				return shellCommandProcessor.sendAndReceive((String) payload);
			}
			else {
				throw new IllegalArgumentException("Only String payloads are supported with no wrapper configured");
			}
		}
	}
}