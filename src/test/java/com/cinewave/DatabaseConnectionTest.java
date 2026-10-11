package com.cinewave;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dedicated database connectivity and schema verification test.
 * Verifies that the configured DataSource successfully establishes a connection,
 * interrogates database metadata, and executes basic queries.
 */
@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDataSourceConnection_AndExecuteQuery() throws Exception {
        assertNotNull(dataSource, "DataSource should be initialized");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Database connection should not be null");
            assertFalse(connection.isClosed(), "Database connection should be open");

            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData.getDatabaseProductName(), "Database product name should be retrievable");

            // Execute connectivity validation query
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT 1")) {
                assertTrue(rs.next(), "Validation query should return a result row");
                assertEquals(1, rs.getInt(1), "Result should be 1");
            }
        }
    }
}
