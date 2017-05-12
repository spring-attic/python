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
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.cloud.stream.app.common.resource.repository.config.GitResourceRepositoryConfiguration;
import org.springframework.cloud.stream.app.python.jython.JythonScriptProperties;
import org.springframework.cloud.stream.app.python.script.ScriptResourceUtils;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.dsl.scripting.Scripts;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.scripting.DefaultScriptVariableGenerator;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A Processor that runs a Jython script.
 *
 * @author David Turanski
 **/
@EnableBinding(Processor.class)
@EnableConfigurationProperties(JythonScriptProperties.class)
@Import(GitResourceRepositoryConfiguration.class)
public class PythonJythonProcessorConfiguration {

	@Autowired(required = false)
	private JGitResourceRepository gitResourceRepository;

	@Autowired
	private JythonScriptProperties properties;

	@Autowired
	private ScriptVariableGenerator scriptVariableGenerator;

	@Bean
	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public MessageProcessor<?> transformer() {
		ScriptResourceUtils.overWriteWrapperScriptForGitIfNecessary(gitResourceRepository, properties);
		return Scripts.script(properties.getScriptResource())
				.lang("python")
				.variableGenerator(this.scriptVariableGenerator).get();
	}

	@Bean(name = "variableGenerator")
	public ScriptVariableGenerator scriptVariableGenerator() throws IOException {
		Map<String, Object> variables = new HashMap<>();
		if (properties.getVariables() != null) {
			String[] props = StringUtils.commaDelimitedListToStringArray(properties.getVariables());
			for (String prop: props) {
				String[] toks = StringUtils.split(prop,"=");
				variables.put(toks[0].trim(),toks[1].trim());
			}
		}
		return new DefaultScriptVariableGenerator(variables);
	}

}