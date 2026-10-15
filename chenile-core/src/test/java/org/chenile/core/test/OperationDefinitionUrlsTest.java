package org.chenile.core.test;

import tools.jackson.databind.ObjectMapper;
import org.chenile.core.model.OperationDefinition;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class OperationDefinitionUrlsTest {
    @Test public void readsLegacyAndAliasJsonIndependentlyOfPropertyOrder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OperationDefinition legacy = mapper.readValue("{\"url\":\"/legacy\"}", OperationDefinition.class);
        assertEquals("/legacy", legacy.getUrl());
        assertEquals(List.of("/legacy"), legacy.getUrls());
        for (String json : List.of(
                "{\"url\":\"/legacy\",\"urls\":[\"/alias\",\"/legacy\",\"/alias\"]}",
                "{\"urls\":[\"/alias\",\"/legacy\",\"/alias\"],\"url\":\"/legacy\"}")) {
            OperationDefinition operation = mapper.readValue(json, OperationDefinition.class);
            assertEquals("/legacy", operation.getUrl());
            assertEquals(List.of("/legacy", "/alias"), operation.getUrls());
            assertEquals("/legacy", mapper.valueToTree(operation).get("url").asText());
        }
        OperationDefinition aliases = mapper.readValue("{\"urls\":[\"/first\",\"/second\"]}", OperationDefinition.class);
        assertEquals("/first", aliases.getUrl());
        assertEquals(List.of("/first", "/second"), aliases.getUrls());
        OperationDefinition neutral = mapper.readValue("{}", OperationDefinition.class);
        assertNull(neutral.getUrl());
        assertTrue(neutral.getUrls().isEmpty());
    }
}
