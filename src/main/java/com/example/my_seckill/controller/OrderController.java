package com.example.my_seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.my_seckill.entity.Order;
import com.example.my_seckill.entity.User;
import com.example.my_seckill.exception.GlobalException;
import com.example.my_seckill.service.IOrderService;
import com.example.my_seckill.vo.OrderDetailVo;
import com.example.my_seckill.vo.RespBean;
import com.example.my_seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @GetMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId) {
        if(user == null)
            throw new GlobalException(RespBeanEnum.SESSION_ERROR);
        OrderDetailVo orderDetailVo = orderService.detail(orderId);
        return RespBean.success(orderDetailVo);
    }
}
