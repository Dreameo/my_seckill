package com.example.my_seckill.vo;

import com.example.my_seckill.entity.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 商品列表页面所有需要显示的内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GoodsVo extends Goods {
    private BigDecimal seckillPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
