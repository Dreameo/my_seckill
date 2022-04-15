package com.example.my_seckill.config;

import com.example.my_seckill.entity.User;
import com.example.my_seckill.service.IUserService;
import com.example.my_seckill.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component // 自定义用户参数
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private IUserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {  // 条件判断，如果符合就会，才会执行resolveArgument方法
        Class<?> clazz = parameter.getParameterType();
        return clazz == User.class; // 看这个类型是不是User类
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeRequest(HttpServletResponse.class);
        String ticket = CookieUtil.getCookieValue(request, "userTicket"); // 得到userTicket
        if (StringUtils.isEmpty(ticket))
            return null;
        return userService.getUserByCookie(ticket, request, response);
    }
}
