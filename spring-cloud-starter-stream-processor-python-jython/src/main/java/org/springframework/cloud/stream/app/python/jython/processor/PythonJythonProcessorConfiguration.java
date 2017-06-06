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

package org.springframework.cloud.stream.app.python.jython.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.cloud.stream.app.common.resource.repository.config.GitResourceRepositoryConfiguration;
import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.cloud.stream.app.python.jython.JythonScriptProperties;
import org.springframework.cloud.stream.app.python.jython.ScriptVariableGeneratorConfiguration;
import org.springframework.cloud.stream.app.python.script.ScriptResourceUtils;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * A Processor that runs a Jython script.
 *
 * @author David Turanski
 **/
@EnableBinding(Processor.class)
@EnableConfigurationProperties(JythonScriptProperties.class)
@Import({ GitResourceRepositoryConfiguration.class, ScriptVariableGeneratorConfiguration.class })
public class PythonJythonProcessorConfiguration {

	@Autowired(required = false)
	private JGitResourceRepository gitResourceRepository;

	@Autowired
	private JythonScriptProperties properties;

	@Autowired
	private JythonScriptExecutor jythonScriptExecutor;

	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public Object transformer(Message<?> message) {
		return jythonScriptExecutor.execute(message);
	}

	@Bean
	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public JythonScriptExecutor jythonScriptExecutor(ScriptVariableGenerator scriptVariableGenerator) {
		if (gitResourceRepository != null) {
			ScriptResourceUtils.overwriteScriptLocationToGitCloneTarget(gitResourceRepository, properties);
		}
		return new JythonScriptExecutor(properties.getScriptResource(), scriptVariableGenerator);
	}

}