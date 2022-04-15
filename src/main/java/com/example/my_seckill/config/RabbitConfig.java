package com.example.my_seckill.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiListing;

@Configuration
public class RabbitConfig {

    private static final String EXCHANGE = "SECKILL_EXCHANGE";
    private static final String QUEUE = "SECKILL_QUEUE";

    @Bean
    public Queue queue() {
//        return new Queue("queue", true); // 可以持久化
        return new Queue(QUEUE, true); // 可以持久化
    }

    @Bean // topic 类型交换机
    public TopicExchange seckill_exchange(){
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding01() {
        return BindingBuilder.bind(queue()).to(seckill_exchange()).with("seckill.#");
    }
}
