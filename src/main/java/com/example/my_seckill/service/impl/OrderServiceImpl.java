package com.example.my_seckill.service.impl;

import com.example.my_seckill.entity.Order;
import com.example.my_seckill.mapper.OrderMapper;
import com.example.my_seckill.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}
