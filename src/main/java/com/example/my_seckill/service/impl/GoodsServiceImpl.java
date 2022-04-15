package com.example.my_seckill.service.impl;

import com.example.my_seckill.entity.Goods;
import com.example.my_seckill.entity.User;
import com.example.my_seckill.mapper.GoodsMapper;
import com.example.my_seckill.service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.my_seckill.utils.MD5Utils;
import com.example.my_seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Time;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoById(Long goodsId) {
        return goodsMapper.findGoodsVoById(goodsId);
    }

    @Override
    public String createUrl(User user, Long goodsId) {
        if(user == null) return null;

        String path = MD5Utils.md5(UUID.randomUUID() + "123456");
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, path, 60, TimeUnit.SECONDS); // 将获取到地址存到redis中去

        return path;
    }

    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if(user == null || StringUtils.isEmpty(path))
            return false;
         String redisSeckillPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return redisSeckillPath.equals(path);
    }

    @Override
    public boolean checkCapcha(User user, Long goodsId, String captcha) {
        if(null == user || goodsId < 0 || StringUtils.isEmpty(captcha))
            return false;
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);

        return captcha.equals(redisCaptcha);
    }
}
