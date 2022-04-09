package com.example.my_seckill.service.impl;

import com.example.my_seckill.entity.Goods;
import com.example.my_seckill.mapper.GoodsMapper;
import com.example.my_seckill.service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
