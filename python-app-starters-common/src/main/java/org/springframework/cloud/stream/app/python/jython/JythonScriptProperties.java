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

package org.springframework.cloud.stream.app.python.jython;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.app.python.script.ScriptProperties;

/**
 * Configuration properties for the Jython wrapper.
 *
 * @author David Turanski
 **/
@ConfigurationProperties(prefix = "jython")
public class JythonScriptProperties extends ScriptProperties {
	/*
	 * Not the cleanest, but works better with document generation
	 */
	public static enum Delimiter {
		COMMA, SPACE, TAB, NEWLINE;

		public String value() {
			switch (this) {
			case COMMA:
				return  ",";
			case TAB:
				return "\t";
			case SPACE:
				return " ";
			case NEWLINE:
				return "\n";
			}
			return ",";
		}
	}

	/**
	 * Variable bindings as a delimited string of name-value pairs, e.g. 'foo=bar,baz=car'.
	 */
	private String variables;

	/**
	 * The variable delimiter.
	 */
	private Delimiter delimiter = Delimiter.COMMA;

	public String getVariables() {
		return variables;
	}

	public void setVariables(String variables) {
		this.variables = variables;
	}

	public Delimiter getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(Delimiter delimiter) {
		this.delimiter = delimiter;
	}

	public String getScript() {
		return super.getScript();
	}
}
