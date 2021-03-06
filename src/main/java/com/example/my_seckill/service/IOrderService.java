package com.example.my_seckill.service;

import com.example.my_seckill.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.my_seckill.entity.User;
import com.example.my_seckill.vo.GoodsVo;
import com.example.my_seckill.vo.OrderDetailVo;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goods);

    OrderDetailVo detail(Long orderId);
}
