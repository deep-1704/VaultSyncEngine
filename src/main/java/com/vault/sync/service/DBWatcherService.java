package com.vault.sync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class DBWatcherService {
    private static final Logger log = LoggerFactory.getLogger(DBWatcherService.class);
    private static final long MAX_SIZE_BYTES = 500L * 1024 * 1024; // 500 MB

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DBWatcherService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Scheduled(fixedDelay = 3600000, initialDelay = 60000)
    public void monitorAndCleanupStorage(){
        try {
            Long currentSize = jdbcTemplate.queryForObject(
                    "SELECT pg_database_size(current_database());",
                    Long.class
            );
            log.info("Current DB size (Bytes): {}", currentSize);

            if (currentSize != null && currentSize >= MAX_SIZE_BYTES) {
                log.warn("DB size threshold exceeded ({} bytes). Purging all tables...", currentSize);

                // 1. Truncate all tables in public schema
                jdbcTemplate.execute("""
                    DO $$
                    DECLARE
                      r RECORD;
                    BEGIN
                      FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE;';
                      END LOOP;
                    END $$;
                """);

                // 2. Reclaim disk space (requires autocommit = true)
                try (
                        Connection conn = dataSource.getConnection();
                        Statement stmt = conn.createStatement()
                ){
                    conn.setAutoCommit(true);
                    stmt.execute("VACUUM FULL;");
                }

                log.info("Database purge and VACUUM FULL completed successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to run DB maintenance check", e);
        }
    }
}
