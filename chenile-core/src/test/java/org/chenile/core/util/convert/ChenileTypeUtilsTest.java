package org.chenile.core.util.convert;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChenileTypeUtilsTest {

    @Test
    void makeParameterizedTypeReferenceHandlesRawClassName() {
        ParameterizedTypeReference<?> typeReference =
                ChenileTypeUtils.makeParameterizedTypeReference("java.lang.String");

        assertNotNull(typeReference);
        assertEquals("java.lang.String", typeReference.getType().getTypeName());
    }

    @Test
    void makeParameterizedTypeReferenceHandlesSerializedClassString() {
        ParameterizedTypeReference<?> typeReference =
                ChenileTypeUtils.makeParameterizedTypeReference("class java.lang.String");

        assertNotNull(typeReference);
        assertEquals("java.lang.String", typeReference.getType().getTypeName());
    }
}
