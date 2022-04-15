package com.example.my_seckill.service;

import com.example.my_seckill.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.my_seckill.vo.LoginVo;
import com.example.my_seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
public interface IUserService extends IService<User> {

    RespBean login(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);

    RespBean updatePass(String userTicket, Long id, String password);

}
