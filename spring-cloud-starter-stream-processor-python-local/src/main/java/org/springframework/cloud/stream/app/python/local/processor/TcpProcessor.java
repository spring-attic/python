package org.springframework.cloud.stream.app.python.local.processor;

import org.springframework.http.MediaType;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.support.MutableMessageBuilder;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.UnsupportedEncodingException;

/**
 * Subclass of {@link TcpOutboundGateway} that converts reply to String if request is a String
 *
 * @author David Turanski
 **/

public class TcpProcessor extends TcpOutboundGateway {

	private final String charset;
	private MediaType contentType;

	public TcpProcessor(String charset) {
		super();
		this.charset = charset;
	}

	public void setContentType(MediaType contentType) {
		this.contentType = contentType;
	}

	@Override
	protected Object handleRequestMessage(Message<?> message) {
		Message<byte[]> reply = (Message<byte[]>) super.handleRequestMessage(message);

		MutableMessageBuilder messageBuilder;

		if (message.getPayload() instanceof String) {

			byte[] bytes = reply.getPayload();
			try {
				messageBuilder = MutableMessageBuilder.withPayload(new String(bytes, charset));
			}
			catch (UnsupportedEncodingException e) {
				throw new MessageTransformationException(e.getMessage(), e);
			}
		}
		else {
			messageBuilder = MutableMessageBuilder.fromMessage(reply);
		}

		if (contentType != null) {
			messageBuilder.setHeader(MessageHeaders.CONTENT_TYPE, contentType);
		}

		return messageBuilder.build();
	}
}
