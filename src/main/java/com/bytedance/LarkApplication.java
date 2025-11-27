package com.bytedance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.bytedance.mapper")
@SpringBootApplication
public class LarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(LarkApplication.class, args);
    }

}
