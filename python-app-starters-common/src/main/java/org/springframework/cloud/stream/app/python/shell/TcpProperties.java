/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.python.shell;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.cloud.stream.app.python.tcp.AbstractTcpConnectionFactoryProperties;
import org.springframework.util.SocketUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Properties for the TCP Client processor.
 *
 * @author Ilayaperumal Gopinathan
 * @author Gary Russell
 * @author David Turanski
 *
 */
@ConfigurationProperties("tcp")
@Validated
public class TcpProperties extends AbstractTcpConnectionFactoryProperties {

	private int port;

	private int monitorPort;

	/**
	 * The buffer size used when decoding messages; larger messages will be rejected.
	 */
	private int bufferSize = 2048;

	/**
	 * The encoder to use when sending messages.
	 */
	private Encoding encoder = Encoding.CRLF;

	/**
	 * The charset used when converting from bytes to String.
	 */
	private String charset = "UTF-8";

	/**
	 * Retry interval (in milliseconds) to check the connection and reconnect.
	 */
	private long retryInterval = 60000;


	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}



	@NotNull
	public Encoding getEncoder() {
		return this.encoder;
	}

	public void setEncoder(Encoding encoder) {
		this.encoder = encoder;
	}

	@NotNull
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public long getRetryInterval() {
		return this.retryInterval;
	}

	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}

	public synchronized int getPort() {
		if (port == 0) {
			port = SocketUtils.findAvailableTcpPort();
		}
		return port;
	}

	public synchronized int getMonitorPort() {
		if (monitorPort == 0) {
			monitorPort = SocketUtils.findAvailableTcpPort();
		}
		return monitorPort;
	}
}
