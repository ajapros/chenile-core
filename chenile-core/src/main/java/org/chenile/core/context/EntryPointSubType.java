package org.chenile.core.context;

public enum EntryPointSubType {
	NONE,
	LOCAL_PROXY,
	REMOTE_PROXY;

	public static EntryPointSubType fromValue(String value) {
		if (value == null || value.isBlank()) {
			return NONE;
		}
		return EntryPointSubType.valueOf(value);
	}
}
