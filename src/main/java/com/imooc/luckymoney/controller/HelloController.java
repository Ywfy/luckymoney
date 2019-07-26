package com.imooc.luckymoney.controller;

import com.imooc.luckymoney.properties.LimitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Controller + @ResponseBody = @RestController
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private LimitConfig limitConfig;


    @PostMapping("/say")
    //@RequestMapping("/say")
    //@ResponseBody
    public String say(@RequestParam(value = "id", required = false, defaultValue = "0")Integer id){
        /*return "good good study，day day up！" +
                " minMoney:" + limitConfig.getMinMoney() +
                " 说明:" + limitConfig.getDescription();*/
        /*return "index";*/
        return "id: " + id;
    }

   /* @GetMapping("/hello2")
    public String say2(){
        return "index";
    }*/
}
