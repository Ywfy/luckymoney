package com.imooc.luckymoney;

import com.imooc.luckymoney.domain.Luckymoney;
import com.imooc.luckymoney.service.LuckymoneyService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LuckymoneyServiceTest {

    @Autowired
    private LuckymoneyService luckymoneyService;

    @Test
    public void findOneTest(){
        Luckymoney luckymoney = luckymoneyService.findOne(13);
        //此处试了一下，发现61后续有多少个0，BigDecimal.valueof方法最后的结果都是61.0
        //网上查了后，发现了Decimalformat这个类
        DecimalFormat decimalformat = new DecimalFormat("#.00");
        Assert.assertEquals(decimalformat.format(61.00), luckymoney.getMoney().toString());
    }

}
