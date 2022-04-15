package com.example.my_seckill.mapper;

import com.example.my_seckill.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.my_seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoById(@Param("goodsId") Long goodsId);
}
