package com.example.my_seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.my_seckill.entity.Order;
import com.example.my_seckill.entity.SeckillOrder;
import com.example.my_seckill.entity.User;
import com.example.my_seckill.exception.GlobalException;
import com.example.my_seckill.rabbitmq.MQSender;
import com.example.my_seckill.rabbitmq.SeckillMessage;
import com.example.my_seckill.service.IGoodsService;
import com.example.my_seckill.service.IOrderService;
import com.example.my_seckill.service.ISeckillOrderService;
import com.example.my_seckill.utils.JsonUtil;
import com.example.my_seckill.vo.GoodsVo;
import com.example.my_seckill.vo.RespBean;
import com.example.my_seckill.vo.RespBeanEnum;
import com.mysql.jdbc.log.Log;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 秒杀商品表 前端控制器
 * </p>
 *
 * @author steve
 * @since 2022-04-09
 */
@Controller
@RequestMapping("/seckill")
public class SeckillGoodsController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private DefaultRedisScript<Long> redisScript;


    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();


    @GetMapping("/captcha")
    @ResponseBody
    public void captcha(User user, Long goodsId, HttpServletRequest request, HttpServletResponse response) throws Exception {

        if(user == null || goodsId < 0)
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);

        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 三个参数分别为宽、高、位数
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        // 设置字体
        specCaptcha.setFont(new Font("Verdana", Font.PLAIN, 32));  // 有默认字体，可以不用设置
        // 设置类型，纯数字、纯字母、字母数字混合
        specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);

        // 验证码存入redis
//        request.getSession().setAttribute("captcha", specCaptcha.text().toLowerCase());
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, specCaptcha.text(), 300, TimeUnit.SECONDS);

        // 输出图片流
        specCaptcha.out(response.getOutputStream());

    }

    @GetMapping("/path")
    @ResponseBody
    public RespBean path(User user, Long goodsId, String captcha) {
        if(user == null)
            return RespBean.error(RespBeanEnum.SESSION_ERROR);

        boolean check = goodsService.checkCapcha(user, goodsId, captcha);
        if(!check)
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);

        String url = goodsService.createUrl(user, goodsId);

        return RespBean.success(url);
    }

    @PostMapping("/{path}/doSeckill")
    @ResponseBody
    public RespBean doPathSeckill(@PathVariable("path") String path, User user, Long goodsId) {
        if(user == null) return RespBean.error(RespBeanEnum.SESSION_ERROR);

        ValueOperations valueOperations = redisTemplate.opsForValue();

        boolean check = goodsService.checkPath(user, goodsId, path);
        if(!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 判断是否重复抢购

        String seckillOrderJson = (String) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if(!StringUtils.isEmpty(seckillOrderJson))
            return RespBean.error(RespBeanEnum.REPEAT_SECKILL); // 重复抢购

        //内存标记,减少Redis访问
        if(EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.STOCK_EMPTY); // 库存为空， 提前返回
        }

        // lua 脚本预 减库存
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if(stock < 0) {
            EmptyStockMap.put(goodsId, true); // 标记此时 库存已 空
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.STOCK_EMPTY); // 返回库存为空
        }

        // 如果不为空，那么就请求入队， 立即返回排队中
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);
    }



    @RequestMapping("/doSeckill")
    @ResponseBody
    public RespBean doSeckill(User user, Long goodsId) {

        if(user == null) return RespBean.error(RespBeanEnum.SESSION_ERROR);


//        GoodsVo goods = goodsService.findGoodsVoById(goodsId);
//
//        // 判断库存是否满足抢购
//        if (goods.getStockCount() < 1) {
////            throw new GlobalException(RespBeanEnum.STOCK_EMPTY);
//            return RespBean.error(RespBeanEnum.STOCK_EMPTY);
//        }

//        // 判断是否重复抢购
//        String SeckillOrderJson = (String) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goods.getId());
//
//        if(!StringUtils.isEmpty(SeckillOrderJson))
//            return RespBean.error(RespBeanEnum.REPEAT_SECKILL);
//            throw new GlobalException(RespBeanEnum.REPEAT_SECKILL);
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
//                .eq("user_id", user.getId())
//                .eq("goods_id", goods.getId()));

//        if(seckillOrder != null) {
//           throw new GlobalException(RespBeanEnum.REPEAT_SECKILL);
//        }

        // 判断是否重复抢购
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String seckillOrderJson = (String) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if(!StringUtils.isEmpty(seckillOrderJson))
            return RespBean.error(RespBeanEnum.REPEAT_SECKILL); // 重复抢购

        //内存标记,减少Redis访问
        if(EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.STOCK_EMPTY); // 库存为空， 提前返回
        }

        // 没有重复抢购，那么就往订单中添加数据
        // 没有重复抢购，那么就在redis中 预减库存

        //redis 预减库存
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
//        if(stock < 0) {
//            EmptyStockMap.put(goodsId, true); // 标记此时 库存已 空
//            valueOperations.increment("seckillGoods:" + goodsId);
//            return RespBean.error(RespBeanEnum.STOCK_EMPTY); // 返回库存为空
//        }



        // lua 脚本预 减库存
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if(stock < 0) {
            EmptyStockMap.put(goodsId, true); // 标记此时 库存已 空
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.STOCK_EMPTY); // 返回库存为空
        }

        // 如果不为空，那么就请求入队， 立即返回排队中
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);

//        Order order = orderService.seckill(user, goods);
//        if(null != order)
//            return RespBean.success(order);
//
//        return RespBean.error(RespBeanEnum.ERROR);


    }

    @GetMapping("/getResult")
    @ResponseBody
    public RespBean result(User user, Long goodsId) {
        if(user == null)
            return RespBean.error(RespBeanEnum.SESSION_ERROR);

        Long orderId = seckillOrderService.getResult(user, goodsId);

        return RespBean.success(orderId);
    }







    @RequestMapping("/doSeckill2")
    public String doSeckill2(User user, Model model, Long goodsId) {
        if(user == null) return "login";
        model.addAttribute("user", user);

        GoodsVo goods = goodsService.findGoodsVoById(goodsId);

        // 判断库存是否满足抢购
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.STOCK_EMPTY.getMessage());
            return "secKillFail";
        }

        // 判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goods.getId()));

        if(seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_SECKILL.getMessage());
            return "secKillFail";
        }

        // 没有重复抢购，那么就往订单中添加数据
        // 没有重复抢购，也还有库存，那么将请求入 队列， 然后返回响应



        Order order = orderService.seckill(user, goods);



        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 系统初始化后，将库存加到redis中
        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(goodsVo))
            return; // 如果没有商品， 直接返回

        goodsVo.forEach(good -> {
            redisTemplate.opsForValue().set("seckillGoods:" + good.getId(), good.getStockCount()); // 初始化时，将库存预加载到redis中去
            EmptyStockMap.put(good.getId(), false); // 标记redis中库存不为空
        });
    }
}
