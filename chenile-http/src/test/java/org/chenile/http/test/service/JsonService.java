package org.chenile.http.test.service;

public interface JsonService {

    JsonData getOne(String id);

    JsonData save(JsonData jsonData);

    JsonData saveWithIdParam(String id, JsonData jsonData);

	JsonData throwException(JsonData jsonData);

	JsonData throwWarning(JsonData jsonData);

    JsonData throwMultipleErrorsInException(JsonData jsonData);
    JsonData ping(JsonData data);
}
