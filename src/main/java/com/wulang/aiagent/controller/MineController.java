package com.wulang.aiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mine")
public class MineController {

    @GetMapping
    public String callMine() {
        return "0ok";
    }
}
