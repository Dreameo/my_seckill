package com.example.my_seckill.exception;

import com.example.my_seckill.vo.RespBean;
import com.example.my_seckill.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class) // 要处理的异常类
    public RespBean ExceptionHandler(Exception e) {
        if(e instanceof GlobalException) { // 异常是不是属于我们刚定义的全局异常
            GlobalException ex = (GlobalException) e;
            return RespBean.error(ex.getRespBeanEnum()); // 我们在程序中定义的异常，这些异常包含了code， message
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);// 参数异常
            respBean.setMessage("参数校验异常：" +
                    ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        return RespBean.error(RespBeanEnum.ERROR); // 服务器异常
    }

}
