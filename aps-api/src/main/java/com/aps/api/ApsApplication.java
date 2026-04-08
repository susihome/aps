package com.aps.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.aps")
@EntityScan("com.aps.domain.entity")
@EnableJpaRepositories("com.aps.service.repository")
public class ApsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApsApplication.class, args);
    }
}
