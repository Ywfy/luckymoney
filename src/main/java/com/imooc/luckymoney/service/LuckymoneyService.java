package com.imooc.luckymoney.service;

import com.imooc.luckymoney.domain.Luckymoney;
import com.imooc.luckymoney.enums.ResultEnum;
import com.imooc.luckymoney.exception.LuckymoneyException;
import com.imooc.luckymoney.repository.LuckymoneyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class LuckymoneyService {

    @Autowired
    private LuckymoneyRepository repository;

    /**
     * 事务 是指数据库事务，底层数据库必须支持事务管理才行
     * 对于MySQL，则引擎必须选择innodb才行，MyISAM是不支持事务的
     */
    @Transactional
    public void createTwo(){
        Luckymoney luckymoney1 = new Luckymoney();
        luckymoney1.setProducer("小星星");
        luckymoney1.setMoney(new BigDecimal(520));
        repository.save(luckymoney1);

        Luckymoney luckymoney2 = new Luckymoney();
        luckymoney2.setProducer("小星星");
        luckymoney2.setMoney(new BigDecimal(1314));
        repository.save(luckymoney2);

    }

    public void getMoney(Integer id) throws Exception{
        Luckymoney luckymoney = repository.findById(id).orElse(null);
        BigDecimal money = luckymoney.getMoney();
        if(money.compareTo(BigDecimal.ONE) < 0){
            //返回"You are so tight"
            throw new LuckymoneyException(ResultEnum.TIGHT);
        }else if(money.compareTo(BigDecimal.TEN) < 0) {
            //返回"Thank you!"
            throw new LuckymoneyException(ResultEnum.THANKS);
        }else if(money.compareTo(BigDecimal.valueOf(100)) < 0){
            //返回"Good job!"
            throw new LuckymoneyException(ResultEnum.GOOD);
        }else if(money.compareTo(BigDecimal.valueOf(100)) >= 0){
            //返回"Have you made a fortune?"
            throw new LuckymoneyException(ResultEnum.GOD);
        }
    }

    /**
     * 通过ID查询，返回对应Luckymoney对象
     * @param id
     * @return
     */
    public Luckymoney findOne(Integer id){
        return repository.findById(id).orElse(null);
    }
}
