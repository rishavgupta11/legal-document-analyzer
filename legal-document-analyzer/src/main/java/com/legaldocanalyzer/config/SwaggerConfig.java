package com.legaldocanalyzer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Legal Document Analyzer API")
                        .version("1.0.0")
                        .description("API for analyzing legal documents and identifying risky clauses")
                        .contact(new Contact()
                                .name("Legal Document Analyzer Team")
                                .email("support@legaldocanalyzer.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server"),
                        new Server().url("https://api.legaldocanalyzer.com").description("Production server")
                ));
    }
}