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

package org.springframework.cloud.stream.app.python.local.wrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.cloud.stream.app.common.resource.repository.config.GitResourceRepositoryConfiguration;
import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.cloud.stream.app.python.jython.ScriptVariableGeneratorConfiguration;
import org.springframework.cloud.stream.app.python.local.tcp.TcpProcessor;
import org.springframework.cloud.stream.app.python.local.tcp.TcpProcessorConfiguration;
import org.springframework.cloud.stream.app.python.script.ScriptResourceUtils;
import org.springframework.cloud.stream.app.python.wrapper.JythonWrapperProperties;
import org.springframework.cloud.stream.shell.ShellCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.scripting.ScriptVariableGenerator;

/**
 * @author David Turanski
 **/
@Configuration
@EnableConfigurationProperties(JythonWrapperProperties.class)
@Import({ GitResourceRepositoryConfiguration.class, ScriptVariableGeneratorConfiguration.class,
		TcpProcessorConfiguration.class })
public class JythonWrapperConfiguration {

	@Configuration
	@ConditionalOnBean(ShellCommand.class)
	@ConditionalOnProperty("wrapper.script")
	static class TcpProcessorJythonWrapperConfig {

		@Autowired(required = false)
		private JGitResourceRepository gitResourceRepository;

		@Autowired
		private JythonWrapperProperties properties;

		@Bean
		public JythonScriptExecutor jythonWrapper(ScriptVariableGenerator variableGenerator, TcpProcessor processor) {
			if (gitResourceRepository != null) {
				ScriptResourceUtils.overwriteScriptLocationToGitCloneTarget(gitResourceRepository, properties);
			}
			return new TcpProcessorJythonWrapper(properties.getScriptResource(), variableGenerator, processor);
		}
	}
}
