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
import org.springframework.cloud.stream.shell.ShellCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

/**
 * @author David Turanski
 **/
@Configuration
@ConditionalOnMissingBean(ShellCommand.class)
@EnableConfigurationProperties({ PythonShellCommandProperties.class, TcpProperties.class })
public class PythonShellCommandConfiguration {

	@Autowired
	private PythonShellCommandProperties properties;

	@Autowired
	private TcpProperties tcpProperties;

	@Autowired(required = false)
	private JGitResourceRepository repository;

	@Bean
	@Profile("!cloud")
	public ShellCommand shellCommand() {
		ShellCommand shellCommand = new ShellCommand(buildCommand());
		return shellCommand;
	}

	@Bean
	@Profile("cloud")
	public ShellCommand cfShellCommand() {
		PythonEnvironmentHelper.installPipIfNecessary(properties.getPipCommandName());
		String scriptName = PythonEnvironmentHelper.wrapCommandforCloud(buildCommand());
		return new ShellCommand(scriptName);

	}

	private String buildCommand() {
		return StringUtils.isEmpty(properties.getArgs()) ?
				StringUtils.join(new String[] { properties.getCommandName(), buildScriptAbsolutePath(), buildTcpArgs(),
				}, " ") :
				StringUtils.join(new String[] { properties.getCommandName(), buildTcpArgs(), buildScriptAbsolutePath(),
						buildTcpArgs(), properties.getArgs() }, " ");
	}

	private String buildTcpArgs() {
		return StringUtils.join(new String[] {
						"--port", String.valueOf(tcpProperties.getPort()),
						"--monitor-port", String.valueOf(tcpProperties.getMonitorPort()),
						"--buffer-size",  String.valueOf(tcpProperties.getBufferSize()),
						"--char-encoding", tcpProperties.getCharset(),
						"--encoder", tcpProperties.getEncoder().name()
				},
				" ");
	}

	private String buildScriptAbsolutePath() {
		String script = null;
		if (repository != null) {
			ScriptResourceUtils.overwriteScriptLocationToGitCloneTarget(repository, properties, properties.getPath());
			script = properties.getScript();
		}
		else {
			script = StringUtils.isEmpty(properties.getPath()) ?
					properties.getScript() :
					properties.getPath() + File.separator + properties.getScript();
		}
		return script;
	}

}
