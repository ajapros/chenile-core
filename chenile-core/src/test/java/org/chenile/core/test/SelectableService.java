package org.chenile.core.test;

public class SelectableService {
	private final String response;

	public SelectableService(String response) {
		this.response = response;
	}

	public String choose() {
		return response;
	}
}
