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

package org.springframework.cloud.stream.app.python.script;

import org.springframework.cloud.stream.app.common.resource.repository.JGitResourceRepository;

import java.io.File;

/**
 * @author David Turanski
 **/
public abstract class ScriptResourceUtils {
	/*
 * Example:
 * wrapper.script = test_apps/upper.py
 * git.url=https://github.com/dturanski/python-apps
 *
 * The JGitRepository is set to clone on start up, the repo has already been cloned to the local file system.
 * That is the path the Jython wrapper will use.
 */
	public static void overwriteScriptLocationToGitCloneTarget(JGitResourceRepository repository,
			ScriptProperties properties) {
		overwriteScriptLocationToGitCloneTarget(repository, properties, null);
	}

	public static void overwriteScriptLocationToGitCloneTarget(JGitResourceRepository repository,
			ScriptProperties properties, String basedir) {
		if (repository != null) {
			String path = basedir == null ?
					repository.getBasedir().getAbsolutePath() + File.separator + properties.getScript() :
					repository.getBasedir().getAbsolutePath() + File.separator + basedir + File.separator + properties.getScript();
			properties.setScript(path);
		}
	}
}
