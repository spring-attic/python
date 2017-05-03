
package org.springframework.cloud.stream.app.common.resource.repository;

/**
 * @author David Turanski
 **/
public class NoSuchBranchException extends RuntimeException {

		public NoSuchBranchException(String string, Exception e) {
			super(string, e);
		}
}
