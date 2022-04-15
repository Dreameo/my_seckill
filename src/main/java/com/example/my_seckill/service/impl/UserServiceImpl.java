package com.example.my_seckill.service.impl;

import com.example.my_seckill.entity.User;
import com.example.my_seckill.exception.GlobalException;
import com.example.my_seckill.mapper.UserMapper;
import com.example.my_seckill.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.my_seckill.utils.CookieUtil;
import com.example.my_seckill.utils.MD5Utils;
import com.example.my_seckill.utils.UUIDUtil;
import com.example.my_seckill.vo.LoginVo;
import com.example.my_seckill.vo.RespBean;
import com.example.my_seckill.vo.RespBeanEnum;
import com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sun.applet.AppletViewer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 参数校验，健壮性判断  ——————> jsr303
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    @Override
    public RespBean login(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile(); // 手机号码
        String password = loginVo.getPassword(); // 密码，前端加过密的

//        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) { // 前端判断了是否为空，后端为了安全起见还是会写这些判断，最后一道防线
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR); // 登录失败： 用户名或者密码不正确
//        }
//
//        // 判断手机号码格式是否正确，jsr303
//        if(!ValidatorUtil.isMobile(mobile))
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR); // 手机格式错误


        User user = userMapper.selectById(mobile);

        if(user == null)
//            return RespBean.error(RespBeanEnum.MOBILE_NOT_EXIST); // 查不到账号，那就是手机号为空了
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);

        if(!MD5Utils.formPassToDbPass(password, user.getSalt()).equals(user.getPassword())) { // 数据库密码与前端加密密码不一致
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw  new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 生成cookie
        String ticket = UUIDUtil.uuid();
//        request.getSession().setAttribute(ticket, user);

        redisTemplate.opsForValue().set("user:" + ticket , user); // 字符串设置,将用户信息存入redis中
        CookieUtil.setCookie(request, response, "userTicket", ticket);

        return RespBean.success(ticket); // 成功
    }

    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(userTicket == null) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);

        if(user != null) { // 如果存在用户，重新设置cookie对象
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    @Override
    public RespBean updatePass(String userTicket, Long id, String password) {
        User user = userMapper.selectById(id);
        if(user == null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST); // 手机号码不存在
        }

        user.setPassword(MD5Utils.inputPassToDbPass(password, user.getSalt()));

        int i = userMapper.updateById(user);
        if(i == 1) {
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
