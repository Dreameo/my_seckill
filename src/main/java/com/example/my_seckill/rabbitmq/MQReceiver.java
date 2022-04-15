package com.example.my_seckill.rabbitmq;

import com.example.my_seckill.entity.User;
import com.example.my_seckill.service.IGoodsService;
import com.example.my_seckill.service.IOrderService;
import com.example.my_seckill.utils.JsonUtil;
import com.example.my_seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import springfox.documentation.spring.web.json.Json;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IGoodsService goodsService;
    
    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitListener(queues = "queue")
    public void receive(Object msg) {
        log.info("接受到的消息：" + msg);
    }

    @RabbitListener(queues = "SECKILL_QUEUE")
    public void receive_seckill_msg(String msg) {
        log.info("接受到的信息：" + msg);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(msg, SeckillMessage.class); // 将发送的 json 字符串  转为对象
        // 然后在队列里进行判断是否可以 秒杀成功
        User user = seckillMessage.getUser();
        Long goodId = seckillMessage.getGoodId();

        // 判断是否有库存
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodId);

        if(goodsVo.getStockCount() < 1)
            return; // 没有库存 返回

        // 判断是否重复抢购
        String seckillGoodsJson = (String) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());

        if (!StringUtils.isEmpty(seckillGoodsJson)) {
            // 如果不为空，那么就是重复 秒杀了
            return;
        }
        orderService.seckill(user, goodsVo);
    }
}
