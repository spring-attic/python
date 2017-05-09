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

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;
import org.springframework.cloud.stream.app.common.resource.repository.config.GitResourceRepositoryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Configuration for {@link PythonAppDeployer}. The configured implementation may install a Python app from the local
 * file system, a Git repository, or a classpath resource.
 *
 * @author David Turanski
 **/
@Configuration
@EnableConfigurationProperties(PythonAppDeployerProperties.class)
@Import(GitResourceRepositoryConfiguration.class)
public class PythonAppDeployerConfiguration {

	@Configuration
	@ConditionalOnBean(JGitResourceRepository.class)
	static class GitPythonAppDeployerConfiguration {
		@Autowired
		private PythonAppDeployerProperties properties;

		@Autowired
		private JGitResourceRepository gitResourceRepository;

		@Bean
		public PythonAppDeployer gitPythonAppDeployer() {
			String baseDir = null;

			baseDir = StringUtils.cleanPath(
						gitResourceRepository.getBasedir() + File.separator + properties.getPath());

			FileSystemPythonAppDeployer pythonAppDeployer = new FileSystemPythonAppDeployer(
					new FileSystemResource(baseDir));

			pythonAppDeployer.setPipCommandName(properties.getPipCommandName());
			return pythonAppDeployer;
		}
	}

	@Configuration
	@ConditionalOnMissingBean(JGitResourceRepository.class)
	static class DefaultPythonAppDeployerConfiguration {

		@Autowired
		private PythonAppDeployerProperties properties;

		@Bean
		public PythonAppDeployer pythonAppDeployer() {
			AbstractPythonAppDeployer pythonAppDeployer = null;

			try {
				String protocol = properties.getBasedir().getURL().getProtocol();
				if (protocol != null && protocol.equals("file")) {
					FileSystemResource baseDir = new FileSystemResource(properties.getBasedir().getFile());
					pythonAppDeployer = new FileSystemPythonAppDeployer(baseDir);
				}
				else {
					ClassPathPythonAppDeployer cpPythonAppDeployer = new ClassPathPythonAppDeployer();
					cpPythonAppDeployer.setSourceDir(properties.getBasedir().getFilename());
					pythonAppDeployer = cpPythonAppDeployer;
				}
			}
			catch (IOException e) {
				throw new BeanCreationException(e.getMessage(), e);
			}

			pythonAppDeployer.setPipCommandName(properties.getPipCommandName());

			return pythonAppDeployer;
		}

	}

}
