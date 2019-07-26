package com.imooc.luckymoney.handler;

import com.imooc.luckymoney.domain.Result;
import com.imooc.luckymoney.exception.LuckymoneyException;
import com.imooc.luckymoney.utils.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionHandle {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);

    @ExceptionHandler(value = LuckymoneyException.class)
    @ResponseBody
    public Result handle(LuckymoneyException e){
        logger.error(e.getMessage());
        return ResultUtil.error(e.getCode(), e.getMessage());
    }
}
