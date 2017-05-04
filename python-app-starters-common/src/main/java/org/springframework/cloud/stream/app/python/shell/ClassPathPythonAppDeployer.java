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

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;

/**
 * An implementation of {@link PythonAppDeployer} that deploys class path resources to the local file system.
 *
 * @author David Turanski
 **/

public class ClassPathPythonAppDeployer extends AbstractPythonAppDeployer {

	private String sourceDir = "python";

	@Override
	protected void doDeploy() throws Exception {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
				this.getClass().getClassLoader());

		for (Resource resource : resolver.getResources(String.format("%s%s%s", "classpath*:", sourceDir, "/**"))) {
			copyResource(resource, sourceDir);
		}
	}

	private void copyResource(Resource resource, String sourceDir) {
		String path = null;
		try {
			path = resource.getFile().getPath();
			path = (path.substring(path.lastIndexOf(sourceDir) + sourceDir.length()));
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		if (resource.isReadable()) {
			String targetFilePath = String.format("%s%s", getAppDirPath(), path);
			log.info(String.format("copying resource to %s", targetFilePath));
			try {
				FileUtils.copyInputStreamToFile(resource.getInputStream(), new File(targetFilePath));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Override the default source directory on the class path. The default is 'python'.
	 *
	 * @param sourceDir
	 */
	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

}