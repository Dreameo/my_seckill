package com.example.my_seckill;

import com.example.my_seckill.entity.Goods;
import com.example.my_seckill.mapper.GoodsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MySeckillApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private GoodsMapper goodsMapper;

    @Test
    void test() {
        List<Goods> goods = goodsMapper.selectList(null);
        System.out.println(goods);
    }

}
