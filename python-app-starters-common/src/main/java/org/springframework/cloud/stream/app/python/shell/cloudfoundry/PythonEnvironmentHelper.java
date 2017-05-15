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

package org.springframework.cloud.stream.app.python.shell.cloudfoundry;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Turanski
 **/
public class PythonEnvironmentHelper {
	private final static Log log = LogFactory.getLog(PythonEnvironmentHelper.class);

	final static String PROFILE_PATH = "./.profile.d/python.sh";

	final static String SCRIPT_FILE_NAME = "python_processor.sh";

	final static String TEMPLATE_FILE_NAME = "cf_script_template";

	public String wrappedCommand(String command) {
		createScriptFile(command);
		return new File(SCRIPT_FILE_NAME).getAbsolutePath();
	}

	public void createScriptFile(String command) {

		Configuration configuration = new Configuration(Configuration.getVersion());
		configuration.setClassForTemplateLoading(this.getClass(), "/");

		try {
			Template template = configuration.getTemplate(TEMPLATE_FILE_NAME);
			File scriptFile = new File(SCRIPT_FILE_NAME);
			Writer file = new FileWriter(scriptFile);
			Map<String, String> model = new HashMap<>();
			model.put("PROFILE_PATH", PROFILE_PATH);
			model.put("COMMAND", command);
			template.process(model, file);
			file.flush();
			file.close();
			log.debug("created command file " + scriptFile.getAbsolutePath());
			Runtime.getRuntime().exec("chmod u+x " +  SCRIPT_FILE_NAME);
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (TemplateException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}
}