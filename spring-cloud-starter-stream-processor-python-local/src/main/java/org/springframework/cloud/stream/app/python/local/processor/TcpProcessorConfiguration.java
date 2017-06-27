package org.springframework.cloud.stream.app.python.local.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.python.shell.PythonShellCommandProperties;
import org.springframework.cloud.stream.app.python.shell.TcpProperties;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory;

/**
 * @author David Turanski
 **/
@EnableBinding(Processor.class)
@EnableConfigurationProperties({ TcpProperties.class, PythonShellCommandProperties.class })
public class TcpProcessorConfiguration {

	@Autowired
	private Processor channels;

	@Autowired
	private TcpProperties properties;

	@Autowired
	private PythonShellCommandProperties scriptProperties;

	@Bean
	public TcpProcessor tcpProcessor(
			@Qualifier("tcpClientConnectionFactory") AbstractConnectionFactory connectionFactory) {
		TcpProcessor tcpProcessor = new TcpProcessor(properties.getCharset());
		tcpProcessor.setConnectionFactory((AbstractClientConnectionFactory) connectionFactory);
		tcpProcessor.setReplyChannel(channels.output());
		tcpProcessor.setContentType(scriptProperties.getContentType());
		tcpProcessor.start();
		return tcpProcessor;
	}
}
