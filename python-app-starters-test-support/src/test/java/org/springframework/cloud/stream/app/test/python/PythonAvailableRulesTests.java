package org.springframework.cloud.stream.app.test.python;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author David Turanski
 **/
public class PythonAvailableRulesTests {

	@Test
	@Ignore
	public void springCloudStreamPythonAvailableRule() throws Exception {
		SpringCloudStreamPythonAvailableRule rule = new SpringCloudStreamPythonAvailableRule();
		rule.obtainResource();
	}
}
