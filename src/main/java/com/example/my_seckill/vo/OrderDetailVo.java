package com.example.my_seckill.vo;

import com.example.my_seckill.entity.Order;
import com.example.my_seckill.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVo {
    private GoodsVo goodsVo;
    private Order order;
}
