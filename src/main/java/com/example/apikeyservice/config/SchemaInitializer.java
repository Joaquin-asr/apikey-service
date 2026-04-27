package com.example.apikeyservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@DependsOn("entityManagerFactory")
public class SchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private final DataSource dataSource;

    public SchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void createUniqueActiveIndex() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    IF NOT EXISTS (
                        SELECT 1 FROM sys.indexes
                        WHERE name = 'UIX_api_keys_one_active_per_client'
                        AND object_id = OBJECT_ID('api_keys')
                    )
                    BEGIN
                        CREATE UNIQUE INDEX UIX_api_keys_one_active_per_client
                        ON api_keys (client_id)
                        WHERE active = '1';
                    END
                    """);
            log.info("[SchemaInitializer] UIX_api_keys_one_active_per_client verificado");
        }
    }
}
