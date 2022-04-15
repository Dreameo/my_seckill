package com.example.my_seckill.controller;

import com.example.my_seckill.entity.User;
import com.example.my_seckill.service.IGoodsService;
import com.example.my_seckill.service.IUserService;
import com.example.my_seckill.service.impl.UserServiceImpl;
import com.example.my_seckill.vo.DetailVo;
import com.example.my_seckill.vo.GoodsVo;
import com.example.my_seckill.vo.RespBean;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IUserService userService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;


    /**
     * 加入页面缓存之前
     * qps：1593
     * @param user
     * @param model
     * @return
     */

    @RequestMapping(value = "/toListBefore", produces = "text/html;charset=utf-8") //页面缓存, 返回html源代码
    @ResponseBody
    public String toListBefore(User user, Model model ) {
//        if(StringUtils.isEmpty(ticket))
//            return "login";

//        User user = (User) session.getAttribute(ticket);
//        User user = userService.getUserByCookie(ticket, request, response);

//        if (user == null)
//            return "login";

        // 2. redis中没有页面缓存，从数据库中取 ，存到redis中
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }


    /**
     *  加入页面缓存之后：
     *  qps: 258
     * @param user
     * @param model
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8") //页面缓存, 返回html源代码
    @ResponseBody
    public String toList(User user, Model model, HttpServletResponse response, HttpServletRequest request ) {
//        if(StringUtils.isEmpty(ticket))
//            return "login";

//        User user = (User) session.getAttribute(ticket);
//        User user = userService.getUserByCookie(ticket, request, response);

//        if (user == null)
//            return "login";

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 页面缓存
        // 1. 去redis中取页面缓存，如果有，返回结果
        String html = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)) {
            return html; // 如果页面不为空，返回结果
        }

        // 2. redis中没有页面缓存，从数据库中取 ，存到redis中
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        // 3. 手动渲染页面
            //将参数加入context中
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);

        if(!StringUtils.isEmpty(html)) { // 如果渲染的html不为空，那么就将其放入redis中去，过期时间为一分钟
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS); // 存入redis中一分钟
        }
        return html;
//        return "goodsList";
    }



    @GetMapping(value = "/toDetail2/{goodsId}", produces = "text/html;charset=utf-8")  // url缓存，页面缓存的一种特殊形式，就是根据goodsId缓存页面
    @ResponseBody
    public String toDetail2(User user, Model model, @PathVariable("goodsId") Long goodsId, HttpServletRequest request, HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if(!StringUtils.isEmpty(html)) {
            return html; // 如果缓存中存在缓存,那么就返回
        }
        GoodsVo goods = goodsService.findGoodsVoById(goodsId);
        Date startDate = goods.getStartDate();
        Date endDate = goods.getEndDate();
        Date nowDate = new Date();
        int seckillStatus = 0; // 默认秒杀还未开始
        int remainSeconds = 0;
        if(nowDate.before(startDate)) {
           // 秒杀还未开始
            remainSeconds = (int)(startDate.getTime() - nowDate.getTime()) / 1000;
        } else if(nowDate.after(endDate)) {
            seckillStatus = 2; // 秒杀结束
        } else {
            seckillStatus = 1; // 正在秒杀
        }
        model.addAttribute("user", user);
        model.addAttribute("goods", goods);
        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        // 手动渲染页面
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);

        if(!StringUtils.isEmpty(html)) { // 不为空，存到redis中去
            valueOperations.set("goodsDetail:" + goodsId, html, 120, TimeUnit.SECONDS);
        }

        return html;


//        return "goodsDetail";
    }

    @GetMapping(value = "/detail/{goodsId}")  // 页面静态化
    @ResponseBody
    public RespBean toDetail(User user, Model model, @PathVariable("goodsId") Long goodsId) {

        GoodsVo goods = goodsService.findGoodsVoById(goodsId);
        Date startDate = goods.getStartDate();
        Date endDate = goods.getEndDate();
        Date nowDate = new Date();
//        int seckillStatus = 0; // 默认秒杀还未开始
        int remainSeconds = 0;
        if(nowDate.before(startDate)) {
            // 秒杀还未开始
            remainSeconds = (int)(startDate.getTime() - nowDate.getTime()) / 1000;
        } else if(nowDate.after(endDate)) {
//            seckillStatus = 2; // 秒杀结束
            remainSeconds = -1; // 秒杀结束
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goods);
//        detailVo.setSecKillStatus(seckillStatus);
        detailVo.setRemainSeconds(remainSeconds);

        return RespBean.success(detailVo);
    }



}
