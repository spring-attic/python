package org.springframework.cloud.stream.app.python.local.processor;

import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.messaging.Message;

import java.io.UnsupportedEncodingException;

/**
 * @author David Turanski
 **/


public class TcpProcessor extends TcpOutboundGateway {

	private final String charset;

	public TcpProcessor(String charset) {
		super();
		this.charset = charset;
	}

	@Override
	protected Object handleRequestMessage(Message<?> message) {
		Object reply = super.handleRequestMessage(message);

		if ( message.getPayload() instanceof String) {
			byte[] bytes = ((Message<byte[]>)reply).getPayload();
			try {
				return new String(bytes, charset);
			}
			catch (UnsupportedEncodingException e) {
				throw new MessageTransformationException(e.getMessage(), e);
			}
		}
		return reply;
	}
}
