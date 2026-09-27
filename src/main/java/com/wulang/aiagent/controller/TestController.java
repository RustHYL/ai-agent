package com.wulang.aiagent.controller;

import com.wulang.aiagent.agent.SelfManus;
import com.wulang.aiagent.common.BaseResponse;
import com.wulang.aiagent.common.ResultUtils;
import com.wulang.aiagent.exception.BusinessException;
import com.wulang.aiagent.exception.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class TestController {

    @RequestMapping("/baseresponse/test")
    public BaseResponse<?> testBaseResponseError(){
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, "测试");
    }


    @RequestMapping("/business/test")
    public String testBusinessError(){
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }

}
