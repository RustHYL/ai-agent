package com.wulang.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebScrapingToolTest {

    @Test
    void scrapeWeb() {
        WebScrapingTool tool = new WebScrapingTool();
        String url = "https://www.baidu.com/s?wd=%E6%9C%89%E8%B6%A3%E5%90%8D%E5%AD%97&rsv_spt=1&rsv_iqid=0xd673e07d002a3eb7&issp=1&f=3&rsv_bp=1&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_dl=ih_1&rsv_enter=1&rsv_sug3=1&rsv_sug1=1&rsv_sug7=001&rsv_sug2=1&rsv_btype=i&rsp=1&rsv_sug9=es_2_1&rsv_sug4=4736&rsv_sug=9";
        String result = tool.scrapeWeb(url);
        Assertions.assertNotNull(result);

    }
}