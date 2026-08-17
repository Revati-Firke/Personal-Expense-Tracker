package com.revati.expensetracker.shared.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ApiRootController {

    @Value("${spring.application.name:expense-tracker}")
    private String applicationName;

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("swagger", "/swagger-ui.html");
        links.put("apiDocs", "/api-docs");
        links.put("health", "/actuator/health");
        links.put("expenses", "/api/v1/expenses");
        links.put("budgets", "/api/v1/budgets");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", applicationName);
        response.put("status", "running");
        response.put("links", links);
        return response;
    }
}
