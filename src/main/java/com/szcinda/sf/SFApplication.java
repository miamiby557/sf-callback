package com.szcinda.sf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {"com.szcinda.sf"}
)
@EnableScheduling
public class SFApplication {
    public SFApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(SFApplication.class, args);
    }
}
