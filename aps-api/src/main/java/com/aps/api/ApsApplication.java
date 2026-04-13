package com.aps.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.aps")
@EntityScan("com.aps.domain.entity")
@EnableJpaRepositories("com.aps.service.repository")
@EnableScheduling
public class ApsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApsApplication.class, args);
    }
}
