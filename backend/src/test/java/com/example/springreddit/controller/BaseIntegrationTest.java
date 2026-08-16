package com.example.springreddit.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for integration tests. Sets up a test PostgreSQL container.
 * Prerequisites: Docker must be running when starting integration tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseIntegrationTest {
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:15-alpine"
    );

    static {
        postgres.start();
    }
}