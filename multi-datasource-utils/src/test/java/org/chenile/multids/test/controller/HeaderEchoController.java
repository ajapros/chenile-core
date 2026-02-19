package org.chenile.multids.test.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ChenileController(value = "headerEchoService", serviceName = "headerEchoService")
public class HeaderEchoController extends ControllerSupport {

    @GetMapping("/test/headers")
    public ResponseEntity<GenericResponse<Map<String, String>>> headers(HttpServletRequest request) {
        return process(request);
    }
}
