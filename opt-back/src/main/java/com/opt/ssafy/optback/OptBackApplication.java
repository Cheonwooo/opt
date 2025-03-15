package com.opt.ssafy.optback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OptBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(OptBackApplication.class, args);
    }

}
