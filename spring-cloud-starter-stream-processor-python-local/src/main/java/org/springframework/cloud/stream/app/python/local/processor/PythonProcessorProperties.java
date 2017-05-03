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

package org.springframework.cloud.stream.app.python.local.processor;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * Properties for the Python Processor.
 *
 * @author David Turanski
 **/
@Validated
@ConfigurationProperties(PythonProcessorProperties.PREFIX)
public class PythonProcessorProperties {
	enum Encoder {LF,CRLF,BINARY}

	static final String PREFIX = "python";

	/**
	 * The python command name, e.g., 'python', 'python3'.
	 */
	private String commandName = "python";

	/**
	 * The pip command name, e.g., 'pip', 'pip3'.
	 */
	private String pipCommandName = "pip";

	/**
	 * The root directory (classpath or file system resource) where the Python app located.
	 */
	private Resource rootPath = new ClassPathResource("python");

	/**
	 * The Python script file name, relative to the rootPath.
	 */
	private String script;

	/**
	 * The Python command line args.
	 */
	private String args = "";

	/**
	 * The encoder to use
	 */
	private Encoder encoder = Encoder.CRLF;

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public String getPipCommandName() {
		return pipCommandName;
	}

	public void setPipCommandName(String pipCommandName) {
		this.pipCommandName = pipCommandName;
	}

	@NotNull
	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public Resource getRootPath() {
		return rootPath;
	}

	public void setRootPath(Resource rootPath) {
		this.rootPath = rootPath;
	}

	@NotNull
	public String getScript() {
		return this.script;
	}

	public Encoder getEncoder() {
		return encoder;
	}

	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	public void setScript(String script) {
		this.script = script;
	}



	public Resource getScriptResource() {
		return resolveResource(script);
	}

	private Resource resolveResource(String resourceName) {
		return new PathMatchingResourcePatternResolver()
				.getResource(StringUtils.join(new String[] { rootPath.getFilename(), resourceName }, File.separator));

	}
}