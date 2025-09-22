package com.hp.imagebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.hp.imagebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class ImageBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageBackendApplication.class, args);
    }

}
