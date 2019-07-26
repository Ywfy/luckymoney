package com.imooc.luckymoney.controller;

import com.imooc.luckymoney.domain.Luckymoney;
import com.imooc.luckymoney.domain.Result;
import com.imooc.luckymoney.repository.LuckymoneyRepository;
import com.imooc.luckymoney.service.LuckymoneyService;
import com.imooc.luckymoney.utils.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
public class LuckymoneyController {

    private static final Logger logger = LoggerFactory.getLogger(LuckymoneyController.class);

    @Autowired
    private LuckymoneyRepository repository;
    @Autowired
    private LuckymoneyService luckymoneyService;

    /**
     * 获取红包列表
     */
    @GetMapping("/luckymoneys")
    public List<Luckymoney> list(){
        logger.info("22222222luckymoneys");
        return repository.findAll();
    }

    /**
     * 创建红包（发红包）
     */
    @PostMapping("/luckymoneys")
    public Result<Luckymoney> create(@Valid Luckymoney luckymoney, BindingResult bindingResult) {

        Result result = new Result();

        if(bindingResult.hasErrors()){
            return ResultUtil.error(1, bindingResult.getFieldError().getDefaultMessage());
        }

        Luckymoney luckymoney1 = repository.save(luckymoney);

        return ResultUtil.success(luckymoney1);
    }

    /**
     * 通过ID查询红包
     */
    @GetMapping("/luckymoneys/{id}")
    public Luckymoney findById(@PathVariable("id")Integer id){
        return repository.findById(id).orElse(null);
    }

    /**
     * 更新红包（领红包）
     */
    @PutMapping("/luckymoneys/{id}")
    public Luckymoney updateById(@PathVariable("id")Integer id,
                                 @RequestParam("receiver")String receiver){
        Optional<Luckymoney> optional = repository.findById(id);
        if (optional.isPresent()){
            Luckymoney luckymoney = optional.get();
            luckymoney.setId(id);
            luckymoney.setReceiver(receiver);
            return repository.save(luckymoney);
        }
        return null;
    }

    @PostMapping("/luckymoneys/two")
    public void createTwo(){
        luckymoneyService.createTwo();
    }

    @GetMapping("/luckymoneys/showMoney/{id}")
    public void getMoney(@PathVariable("id")Integer id) throws Exception{
            luckymoneyService.getMoney(id);
    }
}
