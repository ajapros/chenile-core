package org.chenile.core.model;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ParamDefinitionTest {
    @Test
    void setParamClassPopulatesParamType() {
        ParamDefinition paramDefinition = new ParamDefinition();

        paramDefinition.setParamClass(String.class);

        assertSame(String.class, paramDefinition.getParamClass());
        assertSame(String.class, paramDefinition.getParamType());
    }

    @Test
    void setParamTypeKeepsRawParamClassAvailable() {
        ParamDefinition paramDefinition = new ParamDefinition();
        Type listOfStringType = new ParameterizedTypeReference<List<String>>() { }
                .getType();

        paramDefinition.setParamType(listOfStringType);

        assertSame(List.class, paramDefinition.getParamClass());
        assertEquals(listOfStringType, paramDefinition.getParamType());
    }

    @Test
    void setParamClassAfterParamTypePreservesMetadataType() {
        ParamDefinition paramDefinition = new ParamDefinition();
        Type listOfStringType = new ParameterizedTypeReference<List<String>>() { }
                .getType();

        paramDefinition.setParamType(listOfStringType);
        paramDefinition.setParamClass(Iterable.class);

        assertSame(Iterable.class, paramDefinition.getParamClass());
        assertEquals(listOfStringType, paramDefinition.getParamType());
    }

    @Test
    void serializesParamTypeAndLegacyParamClass() throws Exception {
        ParamDefinition paramDefinition = new ParamDefinition();
        Type listOfStringType = new ParameterizedTypeReference<List<String>>() { }
                .getType();
        paramDefinition.setParamType(listOfStringType);

        assertEquals("java.util.List<java.lang.String>", paramDefinition.getParamTypeAsString());
        assertSame(List.class, paramDefinition.getParamClass());
    }

    @Test
    void deserializesLegacyParamClassPayload() throws Exception {
        ParamDefinition paramDefinition = new ParamDefinition();
        paramDefinition.setParamClass(int.class);

        assertSame(int.class, paramDefinition.getParamClass());
        assertSame(int.class, paramDefinition.getParamType());
    }

    @Test
    void deserializesGenericParamTypePayloadAndPopulatesParamClass() throws Exception {
        ParamDefinition paramDefinition = new ParamDefinition();
        paramDefinition.setParamTypeAsString("java.util.List<java.lang.String>");

        assertSame(List.class, paramDefinition.getParamClass());
        assertEquals("java.util.List<java.lang.String>", paramDefinition.getParamType().getTypeName());
    }
}
