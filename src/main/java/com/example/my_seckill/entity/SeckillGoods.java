package com.example.my_seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 秒杀商品表
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@TableName("t_seckill_goods")
@ApiModel(value = "SeckillGoods对象", description = "秒杀商品表")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SeckillGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("秒杀商品ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("商品ID")
    private Long goodsId;

    @ApiModelProperty("秒杀家")
    private BigDecimal seckillPrice;

    @ApiModelProperty("库存数量")
    private Integer stockCount;

    @ApiModelProperty("秒杀开始时间")
    private Date startDate;

    @ApiModelProperty("秒杀结束时间")
    private Date endDate;

}
