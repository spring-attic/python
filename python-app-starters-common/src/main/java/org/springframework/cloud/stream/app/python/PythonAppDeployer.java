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

package org.springframework.cloud.stream.app.python;

/**
 * Strategy interface for Python App Deployment.
 *
 * @author David Turanski
 **/
public interface PythonAppDeployer {
	/**
	 *
	 * @return the target directory where the Python app files have been installed.
	 */
	String getTargetDir();

	/**
	 *
	 * Deploy the Python app, copying whatever source files to a directory on the local file system.
	 */
	void deploy();
}
