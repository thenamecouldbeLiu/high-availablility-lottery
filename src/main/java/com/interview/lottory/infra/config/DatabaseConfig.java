package com.interview.lottory.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    @Bean
    TransactionTemplate transactionTemplate(
            PlatformTransactionManager transactionManager,
            @Value("${app.database.transaction-timeout-seconds:10}") int timeoutSeconds) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(timeoutSeconds);
        return template;
    }
}
