package com.example.my_seckill.service;

import com.example.my_seckill.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.my_seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
public interface IGoodsService extends IService<Goods> {
    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoById(Long goodsId);
}
