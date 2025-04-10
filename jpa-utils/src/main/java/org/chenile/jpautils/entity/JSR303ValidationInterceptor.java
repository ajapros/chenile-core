package org.chenile.jpautils.entity;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.interceptors.BaseChenileInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;
import org.springframework.validation.Validator;

public class JSR303ValidationInterceptor extends BaseChenileInterceptor {
    @Autowired
    Validator validator;
    @Override
    protected void doPreProcessing(ChenileExchange exchange) {

        Object body = exchange.getBody();

        Errors errors = new SimpleErrors(body,"body");
        validator.validate(body,errors);
    }
}
