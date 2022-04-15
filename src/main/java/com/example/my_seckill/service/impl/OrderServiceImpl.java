package com.example.my_seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.my_seckill.entity.Order;
import com.example.my_seckill.entity.SeckillGoods;
import com.example.my_seckill.entity.SeckillOrder;
import com.example.my_seckill.entity.User;
import com.example.my_seckill.exception.GlobalException;
import com.example.my_seckill.mapper.GoodsMapper;
import com.example.my_seckill.mapper.OrderMapper;
import com.example.my_seckill.mapper.SeckillGoodsMapper;
import com.example.my_seckill.service.IGoodsService;
import com.example.my_seckill.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.my_seckill.service.ISeckillGoodsService;
import com.example.my_seckill.service.ISeckillOrderService;
import com.example.my_seckill.utils.JsonUtil;
import com.example.my_seckill.vo.GoodsVo;
import com.example.my_seckill.vo.OrderDetailVo;
import com.example.my_seckill.vo.RespBeanEnum;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Order seckill(User user, GoodsVo goods) {
        // 秒杀商品库存中 商品减1, 商品减1不是 原子操作， 所以将条件放到 sql语句中
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId()));

//        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);

//        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().set("stock_count", seckillGoods.getStockCount())
//                .eq("id", seckillGoods.getId())
//                .gt("stock_count", 0)); // 用sql语句简单解决超卖问题，但是不能完全解决
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count - 1")
                .eq("goods_id", goods.getId())
                .gt("stock_count", 0));
//
        if(!result) {
            redisTemplate.opsForValue().set("isEmptyStock:" + goods.getId(), "0");
            return null;
        }
//        seckillGoodsMapper.updateById(seckillGoods);


        // 生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());

        orderMapper.insert(order);

        // 秒杀订单生成
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);

        // 将 秒杀订单 存到redis中去
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goods.getId(), JsonUtil.object2JsonStr(seckillOrder));

        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null)
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);

        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoById(order.getGoodsId());

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoodsVo(goodsVo);
        orderDetailVo.setOrder(order);

        return orderDetailVo;
    }
}
