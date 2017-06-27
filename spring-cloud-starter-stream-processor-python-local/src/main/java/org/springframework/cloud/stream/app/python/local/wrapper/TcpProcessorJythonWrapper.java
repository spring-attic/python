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

package org.springframework.cloud.stream.app.python.local.wrapper;

import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.cloud.stream.app.python.local.processor.TcpProcessor;
import org.springframework.cloud.stream.shell.ShellCommand;
import org.springframework.core.io.Resource;
import org.springframework.integration.scripting.ScriptVariableGenerator;

import java.util.Map;

/**
 * Runs a @{link JythonScriptExecutor} implementation to invoke a script with a {@link ShellCommand}
 * bound as a variable named 'processor'.
 *
 * @author David Turanski
 **/
public class TcpProcessorJythonWrapper extends JythonScriptExecutor {

	private TcpProcessor tcpProcessor;

	public TcpProcessorJythonWrapper(Resource script, TcpProcessor tcpProcessor) {
		this(script, null, tcpProcessor);
	}

	public TcpProcessorJythonWrapper(Resource script, ScriptVariableGenerator variableGenerator,
			TcpProcessor tcpProcessor) {
		super(script, variableGenerator);
		this.tcpProcessor = tcpProcessor;
	}

	@Override
	protected void bindStaticVariables(Map<String, Object> variables) {
		variables.put("processor", tcpProcessor);
	}
}
