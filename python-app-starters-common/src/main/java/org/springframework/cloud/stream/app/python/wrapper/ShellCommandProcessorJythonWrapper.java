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

package org.springframework.cloud.stream.app.python.wrapper;

import org.springframework.cloud.stream.app.python.wrapper.JythonWrapper;
import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.core.io.Resource;

import java.util.Map;

/**
 * Runs a @{link JythonWrapper} implementation to invoke a script with a {@link ShellCommandProcessor}
 * bound as a variable named 'processor'.
 *
 * @author David Turanski
 **/
public class ShellCommandProcessorJythonWrapper extends JythonWrapper {


	private ShellCommandProcessor shellCommandProcessor;

	public ShellCommandProcessorJythonWrapper(Resource script, ShellCommandProcessor shellCommandProcessor) {
		super(script);
		this.shellCommandProcessor = shellCommandProcessor;
	}

	@Override
	protected void bindStaticVariables(Map<String, Object> variables) {
		variables.put("processor", shellCommandProcessor);
	}
}
