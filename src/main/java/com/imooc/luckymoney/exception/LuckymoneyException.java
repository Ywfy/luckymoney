package com.imooc.luckymoney.exception;

import com.imooc.luckymoney.enums.ResultEnum;

public class LuckymoneyException extends RuntimeException {
    //提一个小点，抛出RuntimeException事务才能回滚，Exception是不会回滚的

    private Integer code;

    public LuckymoneyException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.code = resultEnum.getCode();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
