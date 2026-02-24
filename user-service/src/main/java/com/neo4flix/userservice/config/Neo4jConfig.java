package com.neo4flix.userservice.config;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Fix : Spring Boot 3 + Neo4j crée deux TransactionManagers (réactif + impératif).
 * On marque le Neo4jTransactionManager comme @Primary pour éviter l'ambiguïté.
 */
@Configuration
public class Neo4jConfig {

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            Driver driver,
            DatabaseSelectionProvider databaseSelectionProvider) {
        return new Neo4jTransactionManager(driver, databaseSelectionProvider);
    }
}
