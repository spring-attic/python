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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.stream.shell.ShellCommand;

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

	final static String CF_PROFILE_PATH = "./.profile.d/python.sh";

	final static String CF_SCRIPT_FILE_NAME = "python_processor.sh";

	final static String CF_SCRIPT_TEMPLATE = "cf_script_template";

	final static String PIP_INSTALL_SCRIPT_TEMPLATE = "pip-install-script-template";

	final static String PIP_INSTALL_SCRIPT_FILE_NAME = "install-pip.sh";

	private PythonEnvironmentHelper() {
	}

	public static String wrapCommandforCloud(String command) {
		createScriptFileForCloud(command);
		return new File(CF_SCRIPT_FILE_NAME).getAbsolutePath();
	}

	public static void createScriptFileForCloud(String command) {

		Configuration configuration = new Configuration(Configuration.getVersion());
		configuration.setClassForTemplateLoading(PythonEnvironmentHelper.class, "/");

		try {
			Template template = configuration.getTemplate(CF_SCRIPT_TEMPLATE);
			File scriptFile = new File(CF_SCRIPT_FILE_NAME);
			Writer file = new FileWriter(scriptFile);
			Map<String, String> model = new HashMap<>();
			model.put("PROFILE_PATH", CF_PROFILE_PATH);
			model.put("COMMAND", command);
			template.process(model, file);
			file.flush();
			file.close();

			if (log.isDebugEnabled()) {
				log.debug("created command file " + scriptFile.getAbsolutePath());
			}

			new ShellCommand("chmod u+x " + scriptFile.getAbsolutePath()).execute();
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (TemplateException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public static String installPipIfNecessary(final String pipCommand) {
		Configuration configuration = new Configuration(Configuration.getVersion());
		configuration.setClassForTemplateLoading(PythonEnvironmentHelper.class, "/");
		File scriptFile = new File(PIP_INSTALL_SCRIPT_FILE_NAME);
		try {
			Template template = configuration.getTemplate(PIP_INSTALL_SCRIPT_TEMPLATE);
			Writer file = new FileWriter(scriptFile);
			Map<String, String> model = new HashMap<>();
			model.put("PIP_COMMAND", pipCommand);
			model.put("HOME", System.getenv("HOME"));
			template.process(model, file);
			file.flush();
			file.close();

			if (log.isDebugEnabled()) {
				log.debug("created command file " + scriptFile.getAbsolutePath());
			}

			new ShellCommand("chmod u+x " + scriptFile.getAbsolutePath()).execute();
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (TemplateException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		ShellCommand pipInstaller = new ShellCommand(scriptFile.getAbsolutePath());
		ShellCommand.CommandResponse response = pipInstaller.execute();

		if (response.exitValue() != 0) {
			throw new RuntimeException(
					String.format("Error encountered installing pip. Exit value=%d [%s]",response.exitValue()
							,response.output()));
		}

		String lines[] = StringUtils.split(response.output(),"\n");
		if (log.isInfoEnabled()) {
			for (String line: lines) {
				log.info("STDOUT " + line);
			}
		}
		return  (lines.length > 0 ) ? lines[lines.length-1].trim() : "";
	}
}