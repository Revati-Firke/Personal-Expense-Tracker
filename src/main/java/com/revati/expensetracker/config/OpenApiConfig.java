package com.revati.expensetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI expenseTrackerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Tracker API")
                        .description("""
                                REST API for tracking personal expenses, monthly budgets, \
                                spending summaries, CSV export, and no-spend streak tracking.

                                Source: https://github.com/Revati-Firke/Personal-Expense-Tracker
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Revati Firke")
                                .url("https://github.com/Revati-Firke/Personal-Expense-Tracker"))
                        .license(new License()
                                .name("All Rights Reserved")
                                .url("https://github.com/Revati-Firke/Personal-Expense-Tracker/blob/main/LICENSE")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local development")
                ));
    }
}
