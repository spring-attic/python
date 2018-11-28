/*
 * Copyright 2018 the original author or authors.
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

package org.springframework.cloud.stream.app.python.http.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.httpclient.processor.HttpclientProcessorFunctionConfiguration;
import org.springframework.cloud.stream.app.python.jython.JythonScriptExecutor;
import org.springframework.cloud.stream.app.python.wrapper.JythonWrapperConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author David Turanski
 **/
@Configuration
@Import({ JythonWrapperConfiguration.class, HttpclientProcessorFunctionConfiguration.class })
@EnableBinding(Processor.class)
public class PythonHttpProcessorConfiguration {

	@Autowired(required = false)
	private JythonScriptExecutor jythonWrapper;

	@Bean
	IntegrationFlow pythonHttpFlow(Processor processor,
		Function<Message<?>, Message<?>> preProcess,
		@Qualifier(HttpclientProcessorFunctionConfiguration.HTTPCLIENT_PROCESSOR_FUNCTION_NAME)
			Function<Message<?>, Object> httpRequest,
		Function<Message<?>, Message<?>> postProcess) {
		return IntegrationFlows.from(processor.input())
			.transform(Message.class, preProcess::apply)
			.transform(Message.class, httpRequest::apply)
			.transform(Message.class, postProcess::apply)
			.channel(processor.output())
			.get();

	}

	@Bean
	public Function<Message<?>, Message<?>> preProcess() {
		return message -> applyJythonWrapper(message, Processor.INPUT);
	}

	@Bean
	public Function<Message<?>, Message<?>> postProcess() {
		return message -> applyJythonWrapper(message, Processor.OUTPUT);
	}

	private Message<?> applyJythonWrapper(Message<?> message, String channelName) {
		if (jythonWrapper == null) {
			return message;
		}

		Map<String, Object> channel = new HashMap<>();
		channel.put("channel", channelName);
		Object result = jythonWrapper.execute(message, channel);
		return (MessageBuilder.createMessage(result, message.getHeaders()));
	}
}
