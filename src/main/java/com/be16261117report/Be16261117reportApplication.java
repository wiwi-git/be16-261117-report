package com.be16261117report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Be16261117reportApplication {

    public static void main(String[] args) {
        SpringApplication.run(Be16261117reportApplication.class, args);
    }

}
