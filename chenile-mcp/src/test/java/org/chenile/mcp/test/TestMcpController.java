package org.chenile.mcp.test;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.annotation.ChenileParamType;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.mcp.model.ChenileMCP;
import org.chenile.mcp.model.ChenilePolymorph;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ChenileController(value = "mcpTestService", serviceName = "mcpTestService")
public class TestMcpController extends ControllerSupport {

    @PostMapping("/mcp/simple")
    @ChenileMCP(name = "simpleTool", description = "Simple MCP tool")
    public ResponseEntity<GenericResponse<String>> simple(HttpServletRequest request,
                                                          @RequestBody SimplePayload payload) {
        return process(request, payload);
    }

    @PostMapping("/mcp/foo/{id}/{eventId}")
    @ChenileMCP(name = "fooTool", description = "Polymorphic MCP tool")
    @ChenilePolymorph("testPolymorphProvider")
    public ResponseEntity<GenericResponse<String>> foo(HttpServletRequest request,
                                                       @PathVariable("id") String id,
                                                       @PathVariable("eventId") String eventId,
                                                       @ChenileParamType(Object.class) @RequestBody String eventPayload) {
        return process(request, id, eventId, eventPayload);
    }
}
