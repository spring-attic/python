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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.stream.shell.ShellCommandProcessor;
import org.springframework.core.io.Resource;
import org.springframework.integration.scripting.jsr223.PythonScriptExecutor;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs a Python wrapper script using embedded Jython Script Engine.
 *
 * @author David Turanski
 **/
public class JythonWrapper {

	private final static Log log = LogFactory.getLog(JythonWrapper.class);

	private final PythonScriptExecutor scriptExecutor = new PythonScriptExecutor();

	private ScriptSource scriptSource;
	private ShellCommandProcessor shellCommandProcessor;

	protected JythonWrapper(){}

	public JythonWrapper(Resource script, ShellCommandProcessor shellCommandProcessor) {
		this.shellCommandProcessor = shellCommandProcessor;
		scriptSource = new ResourceScriptSource(script);
	}

	public Object sendAndRecieve(Object payload) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("payload", payload);
		variables.put("processor", shellCommandProcessor);
		variables.put("javax.script.filename" ,"__main__");
		return scriptExecutor.executeScript(scriptSource, variables);
	}

}
