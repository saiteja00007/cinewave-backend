package com.cinewave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CineWaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(CineWaveApplication.class, args);
    }
}
