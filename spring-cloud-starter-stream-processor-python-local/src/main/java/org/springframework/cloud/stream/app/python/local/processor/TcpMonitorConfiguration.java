package org.springframework.cloud.stream.app.python.local.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.app.python.shell.TcpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory;
import org.springframework.messaging.MessageChannel;

/**
 * @author David Turanski
 **/
@Configuration
@Import(TcpConnectionFactoryConfiguration.class)
@EnableConfigurationProperties(TcpProperties.class)
public class TcpMonitorConfiguration {

	@Autowired
	private TcpProperties properties;

	@Bean
	public MessageChannel monitorInput() {
		return new DirectChannel();
	}

	@Bean
	public MessageChannel monitorOutput() {
		return new DirectChannel();
	}

	@Bean
	public TcpReceivingChannelAdapter monitorAdapter(
			@Qualifier("tcpMonitorConnectionFactory") AbstractConnectionFactory connectionFactory) {
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(connectionFactory);
		adapter.setClientMode(true);
		adapter.setRetryInterval(this.properties.getRetryInterval());
		adapter.setOutputChannel(monitorOutput());
		adapter.setAutoStartup(false);
		return adapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "monitorInput")
	public TcpSendingMessageHandler monitorMessageHandler(
			@Qualifier("tcpMonitorConnectionFactory") AbstractConnectionFactory connectionFactory) {
		TcpSendingMessageHandler sendingMessageHandler = new TcpSendingMessageHandler();
		sendingMessageHandler.setConnectionFactory(connectionFactory);
		return sendingMessageHandler;
	}
}
