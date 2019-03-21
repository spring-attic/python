/*
 * Copyright 2017 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.springframework.cloud.stream.app.python.script;

import org.apache.catalina.webresources.FileResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * Base Configuration properties for a Script.
 *
 * @author David Turanski
 **/
public class ScriptProperties {
	/**
	 * The Python script file name.
	 */
	private String script;

	/**
	 * Sets the Content type header for the outgoing Message.
	 */
	private MediaType contentType;

	public MediaType getContentType() {
		return contentType;
	}

	public void setContentType(MediaType contentType) {
		this.contentType = contentType;
	}

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Resource getScriptResource() {
		return resolveResource(script);
	}

	protected Resource resolveResource(String resourceName) {
		return new PathMatchingResourcePatternResolver().getResource("file:" + resourceName);
	}
}