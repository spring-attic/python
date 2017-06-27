package org.springframework.cloud.stream.app.python.local.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.app.python.shell.TcpProperties;
import org.springframework.cloud.stream.app.python.tcp.EncoderDecoderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ip.config.TcpConnectionFactoryFactoryBean;
import org.springframework.integration.ip.tcp.connection.TcpMessageMapper;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;

/**
 * @author David Turanski
 **/
@Configuration
@EnableConfigurationProperties({ TcpProperties.class })
public class TcpConnectionFactoryConfiguration {

	@Autowired
	private TcpProperties properties;

	@Bean
	public TcpConnectionFactoryFactoryBean tcpClientConnectionFactory(
			@Qualifier("tcpClientEncoder") AbstractByteArraySerializer encoder,
			@Qualifier("tcpClientMapper") TcpMessageMapper mapper,
			@Qualifier("tcpClientDecoder") AbstractByteArraySerializer decoder) throws Exception {
		return connectionFactoryFactoryBean(encoder, mapper, decoder, properties.getPort());

	}

	@Bean
	public TcpConnectionFactoryFactoryBean tcpMonitorConnectionFactory(
			@Qualifier("tcpClientEncoder") AbstractByteArraySerializer encoder,
			@Qualifier("tcpClientMapper") TcpMessageMapper mapper,
			@Qualifier("tcpClientDecoder") AbstractByteArraySerializer decoder) throws Exception {
		return connectionFactoryFactoryBean(encoder, mapper, decoder, properties.getMonitorPort());
	}

	private TcpConnectionFactoryFactoryBean connectionFactoryFactoryBean(AbstractByteArraySerializer encoder,
			TcpMessageMapper mapper, AbstractByteArraySerializer decoder, int port) throws Exception {
		TcpConnectionFactoryFactoryBean factoryBean = new TcpConnectionFactoryFactoryBean();
		factoryBean.setType("client");
		factoryBean.setUsingNio(this.properties.isNio());
		factoryBean.setUsingDirectBuffers(this.properties.isUseDirectBuffers());
		factoryBean.setSerializer(encoder);
		factoryBean.setDeserializer(decoder);
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

	@Bean
	public EncoderDecoderFactoryBean tcpClientDecoder() {
		EncoderDecoderFactoryBean factoryBean = new EncoderDecoderFactoryBean(this.properties.getDecoder());
		factoryBean.setMaxMessageSize(this.properties.getBufferSize());
		return factoryBean;
	}

}
