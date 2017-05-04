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

package org.springframework.cloud.stream.app.python.shell;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArraySingleTerminatorSerializer;

import java.io.File;
import java.io.IOException;

/**
 * @author David Turanski
 **/
@Configuration
@ConditionalOnMissingBean(ShellCommandProcessor.class)
@EnableConfigurationProperties(PythonShellCommandProcessorProperties.class)
public class PythonShellCommandProcessorConfiguration {

	@Autowired
	private PythonShellCommandProcessorProperties properties;

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
				.join(new String[] { pythonAppDeployer.getAppDirPath(), script.getFilename() }, File.separator);
		String command = StringUtils
				.join(new String[] { properties.getCommandName(), properties.getArgs(), filePath }, " ");

		ShellCommandProcessor shellCommandProcessor = new ShellCommandProcessor(serializer(), command);
		shellCommandProcessor.setAutoStart(false);
		return shellCommandProcessor;
	}
}
