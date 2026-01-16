package com.rabbiter.am;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.rabbiter")
@MapperScan("com.rabbiter.am.dao")
public class AttendanceManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceManagerApplication.class, args);
    }

}
