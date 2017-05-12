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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.integration.scripting.jsr223.PythonScriptExecutor;
import org.springframework.messaging.Message;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Executes a Jython script executor to transform data to and from external Python applications.
 *
 * @author David Turanski
 **/
public class JythonScriptExecutor implements InitializingBean {

	protected final Log log = LogFactory.getLog(this.getClass());

	private final PythonScriptExecutor scriptExecutor;

	private final Map<String, Object> variables = new HashMap<>();

	private ScriptSource scriptSource;

	/**
	 * Executes a Python script using a {@link PythonScriptExecutor}.
	 * @param script
	 */
	public JythonScriptExecutor(Resource script) {

		scriptSource = new CachingResourceScriptSource(script);
		scriptExecutor = new PythonScriptExecutor();
	}

	/**
	 * Execute the script
	 *
	 * @param message the incoming message, binding 'payload' and 'headers' to the script context.
	 * @return the result
	 */
	public Object execute(Message<?> message) {
		return this.execute(message, null);
	}

	/**
	 * Execute the script.
	 * @param message the incoming message, binding 'payload' and 'headers' to the script context.
	 * @param vars a map of additonal variables and values to bind to the script context.
	 * @return
	 */
	public Object execute(Message<?> message, final Map<String, Object> vars) {
		if (vars != null) {
			this.variables.putAll(vars);
		}
		this.variables.put("payload", message.getPayload());
		this.variables.put("headers", message.getHeaders());
		return scriptExecutor.executeScript(scriptSource, this.variables);
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

	/**
	 * An implementation of {@link ResourceScriptSource} that caches the String contents and cannot be
	 * modified.
	 *
	 * @author David Turanski
	 **/
	static class CachingResourceScriptSource extends ResourceScriptSource {
		private String scriptBody;

		public CachingResourceScriptSource(Resource resource) {
			super(resource);
		}

		@Override
		public String getScriptAsString() throws IOException {
			if (this.scriptBody == null) {
				this.scriptBody = super.getScriptAsString();
			}
			return this.scriptBody;
		}

		@Override
		public boolean isModified() {
			return false;
		}
	}
}
