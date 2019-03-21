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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.integration.scripting.DefaultScriptVariableGenerator;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.integration.scripting.jsr223.PythonScriptExecutor;
import org.springframework.messaging.Message;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A Jython script executor.
 *
 * @author David Turanski
 **/
public class JythonScriptExecutor implements InitializingBean {
	private final static Log logger = LogFactory.getLog(JythonScriptExecutor.class);
	private final ScriptVariableGenerator variableGenerator;
	private final SimpleStringScriptSource script;
	private final PythonScriptExecutor scriptExecutor;
	private final Map<String, Object> staticVariables = new HashMap<>();

	public JythonScriptExecutor(Resource resource) {
		this(resource, null);
	}

	public JythonScriptExecutor(Resource resource, ScriptVariableGenerator variableGenerator) {

		ScriptSource scriptSource = new ResourceScriptSource(resource);

		try {
			this.script = new SimpleStringScriptSource(scriptSource.getScriptAsString());
		}
		catch (IOException e) {
			throw new IllegalArgumentException(String.format("Cannot access script %s", resource.getFilename()));
		}

		this.scriptExecutor = new PythonScriptExecutor();

		this.variableGenerator = variableGenerator == null ? new DefaultScriptVariableGenerator() : variableGenerator;

		bindStaticVariables(this.staticVariables);
	}


	public Object execute(Message<?> message) {
		return this.execute(message, null);
	}

	/**
	 *
	 * @param message the message.
	 * @param additionalVariables additional bind variables.
	 * @return the result.
	 */
	public Object execute(Message<?> message, Map<String, Object> additionalVariables) {
		Map<String, Object> variables = variableGenerator.generateScriptVariables(message);
		variables.putAll(this.staticVariables);
		if (additionalVariables != null) {
			variables.putAll(additionalVariables);
		}
		return this.scriptExecutor.executeScript(this.script, variables);
	}

	public String getScript() {
		return this.script.getScriptAsString();
	}

	protected void bindStaticVariables(Map<String, Object> variables) {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		bindStaticVariables(this.staticVariables);
	}

	static class SimpleStringScriptSource implements ScriptSource {

		private final String script;

		SimpleStringScriptSource(String script) {
			this.script = script;
		}

		@Override
		public String getScriptAsString() {
			return script;
		}

		@Override
		public boolean isModified() {
			return false;
		}

		@Override
		public String suggestedClassName() {
			return SimpleStringScriptSource.class.getName();
		}
	}
}
