package com.example.my_seckill.service;

import com.example.my_seckill.entity.SeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.my_seckill.entity.User;

/**
 * <p>
 * 秒杀订单表 服务类
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);

}
