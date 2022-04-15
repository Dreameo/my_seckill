package com.example.my_seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@TableName("t_user")
@ApiModel(value = "User对象", description = "用户表")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID,手机号码")
    private Long id;

    private String nickname;

    @ApiModelProperty("MD5(MD5(pass明文+固定salt)+salt)")
    private String password;

    private String salt;

    @ApiModelProperty("头像")
    private String head;

    @ApiModelProperty("注册时间")
    private Date registerDate;

    @ApiModelProperty("最后一次登录事件")
    private Date lastLoginDate;

    @ApiModelProperty("登录次数")
    private Integer loginCount;
}
