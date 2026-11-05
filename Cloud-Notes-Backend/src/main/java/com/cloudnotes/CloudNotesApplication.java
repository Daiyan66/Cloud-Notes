package com.cloudnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CloudNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudNotesApplication.class, args);
    }
}
