package org.chenile.core.test;

import org.chenile.core.annotation.ChenileOperation;
import org.chenile.http.annotation.ChenileController;

@ChenileController(
		value = "selectorController",
		serviceName = "defaultSelectableService",
		serviceSelector = "testServiceSelector")
public class ServiceSelectorController {
	@ChenileOperation
	public String choose() {
		return "controller";
	}
}
