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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.integration.scripting.jsr223.PythonScriptExecutor;
import org.springframework.messaging.Message;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Executes a Jython wrapper to transform data to and from external Python applications.
 *
 * @author David Turanski
 **/
public class JythonWrapper implements InitializingBean {
	protected final Log log = LogFactory.getLog(this.getClass());

	private final PythonScriptExecutor scriptExecutor = new PythonScriptExecutor();

	private final Map<String, Object> variables = new HashMap<>();

	private ScriptSource scriptSource;

	public JythonWrapper(Resource script) {
		scriptSource = new ResourceScriptSource(script);
	}

	public Object execute(Message<?> message) {
		variables.put("payload", message.getPayload());
		variables.put("headers", message.getHeaders());
		return scriptExecutor.executeScript(scriptSource, variables);
	}

	protected Object executeWithVariables(Message<?> message, final Map<String, Object> variables) {
		this.variables.putAll(variables);
		return execute(message);
	}

	/**
	 * Subclasses override this to bind static variables required for the script.
	 * @param variables
	 */
	protected void bindStaticVariables(final Map<String, Object> variables) {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		bindStaticVariables(variables);
	}


	/* for testing */
	public ScriptSource getScriptSource() {
		return this.scriptSource;
	}
}
