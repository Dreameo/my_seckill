package com.example.my_seckill.controller;

import com.example.my_seckill.entity.User;
import com.example.my_seckill.rabbitmq.MQSender;
import com.example.my_seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MQSender mqSender;

    @ResponseBody
    @GetMapping("/info")
    public RespBean userInfo(User user) {
        return RespBean.success(user);
    }


    @GetMapping("/mq")
    @ResponseBody
    public void mq() {
        mqSender.send("hello rabbitmq");
    }
}
