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

import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;

/**
 * a {@link PythonAppDeployer} for an app already on the local file system. This doesn't need to move any files, just
 * delegate to the base class to install any dependencies.
 *
 * @author David Turanski
 **/
public class FileSystemPythonAppDeployer extends AbstractPythonAppDeployer {

	/**
	 *
	 * @param appDir the file system location of the Python app.
	 */
	public FileSystemPythonAppDeployer(FileSystemResource appDir) {
		super(appDir);
	}

	@Override
	protected void doDeploy() throws Exception {
	}
}
