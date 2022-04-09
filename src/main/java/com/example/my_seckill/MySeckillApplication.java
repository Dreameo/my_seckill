package com.example.my_seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.my_seckill.mapper")
public class MySeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySeckillApplication.class, args);
    }

}
