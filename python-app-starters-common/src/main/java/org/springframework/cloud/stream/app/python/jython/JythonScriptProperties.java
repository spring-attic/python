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

package org.springframework.cloud.stream.app.python.jython;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.app.python.script.ScriptProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Configuration properties for the Jython wrapper.
 *
 * @author David Turanski
 **/
@ConfigurationProperties(prefix = "jython")
@Validated
public class JythonScriptProperties extends ScriptProperties {
	/**
	 * Variable bindings as a comma delimited string of name-value pairs, e.g. 'foo=bar,baz=car'.
	 */
	private String variables;

	public String getVariables() {
		return variables;
	}

	public void setVariables(String variables) {
		this.variables = variables;
	}

	@NotNull
	public String getScript() {
		return super.getScript();
	}
}
