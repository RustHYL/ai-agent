package com.wulang.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class SelfManusTest {

    @Resource
    private SelfManus selfManus;

    @Test
    void run(){
        String userPrompt = """
                我的另一半居住在上海静安区，请帮我找到5公里内合适的约会地点，地点要求以自然环境为主
                并结合一些网络图片，制定一份详细的约会计划，约会时间两日，并且安排中晚餐
                并以PDF 格式输出
                """;

        String result = selfManus.run(userPrompt);
        Assertions.assertNotNull(result);
    }

}