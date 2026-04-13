package com.aps.mq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aps")
public class ApsMqConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApsMqConsumerApplication.class, args);
    }
}
