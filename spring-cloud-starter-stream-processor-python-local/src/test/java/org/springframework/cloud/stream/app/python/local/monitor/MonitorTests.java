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

package org.springframework.cloud.stream.app.python.local.monitor;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.stream.app.python.local.processor.PythonLocalProcessorConfiguration;
import org.springframework.cloud.stream.app.test.python.SpringCloudStreamPythonAvailableRule;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public abstract class MonitorTests {

	@ClassRule
	public static SpringCloudStreamPythonAvailableRule springCloudStreamPythonAvailableRule = new SpringCloudStreamPythonAvailableRule();

	@Autowired
	PythonProcessHealthIndicator pythonProcessHealthIndicator;

	@Value("${local.server.port}")
	int localPort;


	@TestPropertySource(properties = {
			"security.basic.enabled=false",
			"python.basedir=src/test/resources/python",
			"python.script=processor_example.py" })
	public static class TestPythonHealthIndicatorIsUp extends MonitorTests {
		@Test
		public void indicatorShouldBeUp() throws Exception {
			Health.Builder builder = new Health.Builder();
			pythonProcessHealthIndicator.doHealthCheck(builder);
			assertThat(builder.build()).isEqualTo(Health.up().build());
		}

		@Test
		public void healthEndpointShouldBeUp(){
			RestTemplate restTemplate = new RestTemplate();
			String uri = String.format("http://localhost:%d/health",localPort);
			ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo("{\"status\":\"UP\"}");
		}
	}

	@TestPropertySource(properties = {
			"security.basic.enabled=false",
			"python.basedir=src/test/resources/python",
			"python.script=does_not_exist.py" })
	public static class TestPythonHealthIndicatorIsDown extends MonitorTests {
		@Test
		public void indicatorShouldBeDown() throws Exception {
			Health.Builder builder = new Health.Builder();
			pythonProcessHealthIndicator.doHealthCheck(builder);
			assertThat(builder.build()).isEqualTo(Health.down().build());
		}

		@Test(expected = HttpServerErrorException.class)
		public void healthEndpointShouldBeDown(){
			RestTemplate restTemplate = new RestTemplate();
			String uri = String.format("http://localhost:%d/health",localPort);
			restTemplate.getForEntity(uri, String.class);
		}
	}

	@SpringBootApplication
	@Import(PythonLocalProcessorConfiguration.class)
	public static class PythonApplication {

	}
}
