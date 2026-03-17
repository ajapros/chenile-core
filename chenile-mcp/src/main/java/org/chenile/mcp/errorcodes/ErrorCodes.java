package org.chenile.mcp.errorcodes;

/**
 * Chenile MQTT error codes.
 */
public enum ErrorCodes {

	MISCONFIGURATION(900);

	final int subError;
	private ErrorCodes(int subError) {
		this.subError = subError;
	}
	
	public int getSubError() {
		return this.subError;
	}
}
