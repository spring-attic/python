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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.cloud.stream.app.common.resource.repository.config.GitResourceRepositoryConfiguration;
import org.springframework.cloud.stream.app.python.script.ScriptResourceUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Configuration for {@link PythonAppDeployer} to install a Python app from
 * a Git repository.
 *
 * @author David Turanski
 **/
@Configuration
@EnableConfigurationProperties(PythonAppDeployerProperties.class)
@Import(GitResourceRepositoryConfiguration.class)
@ConditionalOnBean(JGitResourceRepository.class)
public class PythonGitAppDeployerConfiguration {
	@Bean
	public PythonAppDeployer pythonGitAppDeployer(PythonAppDeployerProperties properties,
			JGitResourceRepository gitResourceRepository) {

		String baseDir = StringUtils
				.cleanPath(gitResourceRepository.getBasedir() + File.separator + properties.getPath());

		FileSystemPythonAppDeployer pythonAppDeployer = new FileSystemPythonAppDeployer(
				new FileSystemResource(baseDir));

		pythonAppDeployer.setPipCommandName(properties.getPipCommandName());
		ScriptResourceUtils.overwriteScriptLocationToGitCloneTarget(gitResourceRepository, properties);
		return pythonAppDeployer;
	}
}
