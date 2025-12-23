package org.chenile.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the MIME type supported by the particular service.
 */
public enum MimeType {
	JSON("application/json") , TEXT("text/plain"), HTML("text/html"), PDF("application/pdf");
	private String type;
	private MimeType(String mimeType){
		this.type = mimeType;
	}

	@JsonValue
	public String getType() {
		return type;
	}

	@JsonCreator
	public static MimeType from(String value) {
		for (MimeType mt : values()) {
			if (mt.name().equalsIgnoreCase(value) ||
					mt.type.equalsIgnoreCase(value)) {
				return mt;
			}
		}
		throw new IllegalArgumentException("Unknown MimeType: " + value);
	}

}