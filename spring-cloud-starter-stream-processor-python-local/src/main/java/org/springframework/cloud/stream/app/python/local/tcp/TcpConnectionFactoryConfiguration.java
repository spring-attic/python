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

package org.springframework.cloud.stream.app.python.local.tcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.app.python.shell.TcpProperties;
import org.springframework.cloud.stream.app.python.tcp.EncoderDecoderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ip.config.TcpConnectionFactoryFactoryBean;
import org.springframework.integration.ip.tcp.connection.TcpMessageMapper;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLfSerializer;

/**
 * @author David Turanski
 **/
@Configuration
@EnableConfigurationProperties({ TcpProperties.class })
public class TcpConnectionFactoryConfiguration {

	@Autowired
	private TcpProperties properties;

	@Bean
	public TcpConnectionFactoryFactoryBean tcpClientConnectionFactory(AbstractByteArraySerializer encoder,
			TcpMessageMapper mapper) throws Exception {
		return connectionFactoryFactoryBean(encoder, mapper, properties.getPort());

	}

	@Bean
	public TcpConnectionFactoryFactoryBean tcpMonitorConnectionFactory(AbstractByteArraySerializer encoder,
			TcpMessageMapper mapper) throws Exception {
		return connectionFactoryFactoryBean(new ByteArrayLfSerializer(), mapper, properties.getMonitorPort());
	}

	private TcpConnectionFactoryFactoryBean connectionFactoryFactoryBean(AbstractByteArraySerializer encoder,
			TcpMessageMapper mapper, int port) throws Exception {
		TcpConnectionFactoryFactoryBean factoryBean = new TcpConnectionFactoryFactoryBean();
		factoryBean.setType("client");
		factoryBean.setUsingNio(this.properties.isNio());
		factoryBean.setUsingDirectBuffers(this.properties.isUseDirectBuffers());
		factoryBean.setSerializer(encoder);
		factoryBean.setDeserializer(encoder);
		factoryBean.setSoTimeout(this.properties.getSocketTimeout());
		factoryBean.setMapper(mapper);
		factoryBean.setSingleUse(Boolean.FALSE);
		factoryBean.setHost("localhost");
		factoryBean.setPort(port);
		return factoryBean;

	}

	@Bean
	public EncoderDecoderFactoryBean tcpClientEncoder() {
		return new EncoderDecoderFactoryBean(this.properties.getEncoder());
	}

	@Bean
	public TcpMessageMapper tcpClientMapper() {
		TcpMessageMapper mapper = new TcpMessageMapper();
		mapper.setCharset(this.properties.getCharset());
		return mapper;
	}

}
