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

package org.springframework.cloud.stream.shell;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/
public class ShellCommandTests {

	@Test
	public void test() throws Exception {
		ShellCommand ls = new ShellCommand("ls");
		ShellCommand.CommandResponse response = ls.execute();
		assertThat(response.exitValue()).isZero();
		assertThat(response.output()).contains("pom.xml","src");
	}
}
