package org.example.ekyc.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class KycExecutionConfig {

    @Bean
    public Executor kycExecutor() {
        return Executors.newFixedThreadPool(8);
    }
}
