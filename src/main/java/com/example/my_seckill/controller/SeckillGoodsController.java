package com.example.my_seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.my_seckill.entity.Order;
import com.example.my_seckill.entity.SeckillOrder;
import com.example.my_seckill.entity.User;
import com.example.my_seckill.exception.GlobalException;
import com.example.my_seckill.rabbitmq.MQSender;
import com.example.my_seckill.rabbitmq.SeckillMessage;
import com.example.my_seckill.service.IGoodsService;
import com.example.my_seckill.service.IOrderService;
import com.example.my_seckill.service.ISeckillOrderService;
import com.example.my_seckill.utils.JsonUtil;
import com.example.my_seckill.vo.GoodsVo;
import com.example.my_seckill.vo.RespBean;
import com.example.my_seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * <p>
 * 秒杀商品表 前端控制器
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Controller
@RequestMapping("/seckill")
public class SeckillGoodsController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;


    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    @RequestMapping("/doSeckill")
    @ResponseBody
    public RespBean doSeckill(User user, Long goodsId) {

        if(user == null) return RespBean.error(RespBeanEnum.SESSION_ERROR);


//        GoodsVo goods = goodsService.findGoodsVoById(goodsId);
//
//        // 判断库存是否满足抢购
//        if (goods.getStockCount() < 1) {
////            throw new GlobalException(RespBeanEnum.STOCK_EMPTY);
//            return RespBean.error(RespBeanEnum.STOCK_EMPTY);
//        }

//        // 判断是否重复抢购
//        String SeckillOrderJson = (String) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goods.getId());
//
//        if(!StringUtils.isEmpty(SeckillOrderJson))
//            return RespBean.error(RespBeanEnum.REPEAT_SECKILL);
//            throw new GlobalException(RespBeanEnum.REPEAT_SECKILL);
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
//                .eq("user_id", user.getId())
//                .eq("goods_id", goods.getId()));

//        if(seckillOrder != null) {
//           throw new GlobalException(RespBeanEnum.REPEAT_SECKILL);
//        }

        // 判断是否重复抢购
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String seckillOrderJson = (String) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if(!StringUtils.isEmpty(seckillOrderJson))
            return RespBean.error(RespBeanEnum.REPEAT_SECKILL); // 重复抢购

        //内存标记,减少Redis访问
        if(EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.STOCK_EMPTY); // 库存为空， 提前返回
        }

        // 没有重复抢购，那么就往订单中添加数据
        // 没有重复抢购，那么就在redis中 预减库存

        //预减库存
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if(stock < 0) {
            EmptyStockMap.put(goodsId, true); // 标记此时 库存已 空
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.STOCK_EMPTY); // 返回库存为空
        }

        // 如果不为空，那么就请求入队， 立即返回排队中
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);

//        Order order = orderService.seckill(user, goods);
//        if(null != order)
//            return RespBean.success(order);
//
//        return RespBean.error(RespBeanEnum.ERROR);


    }

    @GetMapping("/getResult")
    @ResponseBody
    public RespBean result(User user, Long goodsId) {
        if(user == null)
            return RespBean.error(RespBeanEnum.SESSION_ERROR);

        Long orderId = seckillOrderService.getResult(user, goodsId);

        return RespBean.success(orderId);
    }







    @RequestMapping("/doSeckill2")
    public String doSeckill2(User user, Model model, Long goodsId) {
        if(user == null) return "login";
        model.addAttribute("user", user);

        GoodsVo goods = goodsService.findGoodsVoById(goodsId);

        // 判断库存是否满足抢购
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.STOCK_EMPTY.getMessage());
            return "secKillFail";
        }

        // 判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goods.getId()));

        if(seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_SECKILL.getMessage());
            return "secKillFail";
        }

        // 没有重复抢购，那么就往订单中添加数据
        // 没有重复抢购，也还有库存，那么将请求入 队列， 然后返回响应



        Order order = orderService.seckill(user, goods);



        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 系统初始化后，将库存加到redis中
        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(goodsVo))
            return; // 如果没有商品， 直接返回

        goodsVo.forEach(good -> {
            redisTemplate.opsForValue().set("seckillGoods:" + good.getId(), good.getStockCount()); // 初始化时，将库存预加载到redis中去
            EmptyStockMap.put(good.getId(), false); // 标记redis中库存不为空
        });
    }
}
