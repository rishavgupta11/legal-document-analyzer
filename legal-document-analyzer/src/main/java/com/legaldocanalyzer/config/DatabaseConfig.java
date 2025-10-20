package com.legaldocanalyzer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.legaldocanalyzer.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Additional database configuration can be added here
}