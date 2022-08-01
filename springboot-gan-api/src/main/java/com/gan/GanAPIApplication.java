
package com.gan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.gan.dao")
@SpringBootApplication
public class GanAPIApplication {

    public static void main(String[] args) {
        SpringApplication.run(GanAPIApplication.class, args);
    }

}
