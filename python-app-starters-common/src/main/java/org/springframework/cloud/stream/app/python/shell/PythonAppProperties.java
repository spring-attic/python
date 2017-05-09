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

import org.springframework.cloud.stream.app.python.script.ScriptProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * Configuration properties for a Python app which may include modules under a base directory, requirements.txt as well
 * as the main script.
 *
 * @author David Turanski
 **/
@Validated
public class PythonAppProperties extends ScriptProperties {
	/**
	 * The root path of Python app. If given, the script path must be relative to this location.
	 */
	private Resource basedir;

	/**
	 * The pip command name, e.g., 'pip', 'pip3'.
	 */
	private String pipCommandName = "pip";

	public Resource getBasedir() {
		return basedir;
	}

	public void setBasedir(Resource basedir) {
		this.basedir = basedir;
	}

	protected Resource resolveResource(String resourceName) {
		if (basedir != null) {
			return new PathMatchingResourcePatternResolver().getResource(basedir.getFilename() + File.separator + resourceName);
		}
		return super.resolveResource(resourceName);
	}

	public String getPath() {
		if (basedir instanceof FileSystemResource) {
			return ((FileSystemResource) basedir).getPath();
		}
		else if (basedir instanceof ClassPathResource) {
			return ((ClassPathResource) basedir).getPath();
		}
		return null;
	}

	@NotNull
	public String getPipCommandName() {
		return pipCommandName;
	}

	public void setPipCommandName(String pipCommandName) {
		this.pipCommandName = pipCommandName;
	}
}
