package com.elmangusto.carrental;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.TimeZone;

@Testcontainers
@ActiveProfiles("test")
public class AbstractPostgresContainerTest {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.1");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
