package org.chenile.mcp.errorcodes;

/**
 * Chenile MQTT error codes.
 */
public enum ErrorCodes {

	MISCONFIGURATION("900");

	final String subError;
	private ErrorCodes(String subError) {
		this.subError = subError;
	}
	
	public String getSubError() {
		return this.subError;
	}
}
