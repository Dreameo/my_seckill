package com.example.my_seckill.vo;

import lombok.*;
import org.springframework.scheduling.support.SimpleTriggerContext;

@AllArgsConstructor
@ToString
@Getter
public enum RespBeanEnum {
    //通用
    SUCCESS(200, "SUCCESS"),
    ERROR(500, "服务端异常"),

    // 登录模块
    LOGIN_ERROR(500210, "用户名或者密码不正确"),
    MOBILE_ERROR(500211, "手机号码格式不正确"),
    BIND_ERROR(500212, "参数校验异常"),
    MOBILE_NOT_EXIST(500213, "手机号码不存在"),
    PASSWORD_UPDATE_FAIL(500214, "更新密码失败"),
    SESSION_ERROR(500215, "用户SESSION不存在"),

    // 订单模块
    ORDER_NOT_EXIST(500300, "订单不存在"),

    // 秒杀模块
    STOCK_EMPTY(500200, "库存为空"),
    REPEAT_SECKILL(500201, "同一用户重复抢购"),

    REQUEST_ILLEGAL(500301, "请求路径非法"),
    ERROR_CAPTCHA(500302,"验证码错误" );

    private final Integer code;
    private final String message;


}
