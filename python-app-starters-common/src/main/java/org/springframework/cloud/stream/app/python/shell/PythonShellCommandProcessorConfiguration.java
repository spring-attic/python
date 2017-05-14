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
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.cloud.stream.app.python.script.ScriptResourceUtils;
import org.springframework.cloud.stream.app.python.shell.cloudfoundry.PythonEnvironmentHelper;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArraySingleTerminatorSerializer;

/**
 * @author David Turanski
 **/
@Configuration
@ConditionalOnMissingBean(ShellCommandProcessor.class)
@EnableConfigurationProperties(PythonShellCommandProcessorProperties.class)
public class PythonShellCommandProcessorConfiguration {

	private final static byte BINARY_ENCODER_X1A = (byte) 26;

	@Autowired
	private PythonShellCommandProcessorProperties properties;

	@Bean
	public AbstractByteArraySerializer serializer() {

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
	@Profile("!cloud")
	public ShellCommandProcessor shellCommandProcessor(AbstractByteArraySerializer serializer,
			JGitResourceRepository repository) {
		if (repository != null) {
			ScriptResourceUtils.overwriteScriptLocationToGitCloneTarget(repository, properties, properties.getPath());
		}

		String command = StringUtils
				.join(new String[] { properties.getCommandName(), properties.getArgs(), properties.getScript() }, " ");

		ShellCommandProcessor shellCommandProcessor = new ShellCommandProcessor(serializer, command);
		shellCommandProcessor.setAutoStart(false);
		return shellCommandProcessor;
	}

	@Bean
	@Profile("cloud")
	public ShellCommandProcessor cfShellCommandProcessor(AbstractByteArraySerializer serializer,
			JGitResourceRepository repository) {
		if (repository != null) {
			ScriptResourceUtils.overwriteScriptLocationToGitCloneTarget(repository, properties, properties.getPath());
		}

		String command = StringUtils
				.join(new String[] { properties.getCommandName(), properties.getArgs(), properties.getScript() }, " ");
		String scriptName =  new PythonEnvironmentHelper().wrappedCommand(command);
		ShellCommandProcessor shellCommandProcessor = new ShellCommandProcessor(serializer, scriptName);

		shellCommandProcessor.setAutoStart(false);
		return shellCommandProcessor;
	}


}
