package com.wulang.aiagent.controller;

import com.wulang.aiagent.agent.SelfManus;
import com.wulang.aiagent.app.LoveApp;
import com.wulang.aiagent.common.BaseResponse;
import com.wulang.aiagent.common.ResultUtils;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import com.wulang.aiagent.exception.ErrorCode;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    @GetMapping("/love_app/chat/sync")
    public BaseResponse<String> doChatWithLoveAppSync(String message, String chatId) {
        String result = loveApp.doChat(message, chatId);
        return ResultUtils.success(result);
    }


    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BaseResponse<String>> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
        // 每条数据包装成 BaseResponse 
        .map(ResultUtils::success) 
        // 流中出错：发一条错误响应后正常结束流 
        .onErrorResume(e -> Flux.just(ResultUtils.error(ErrorCode.SYSTEM_ERROR , e.getMessage())));
    }

    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<BaseResponse<String>>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<BaseResponse<String>>builder()
                        .event("message")
                        .data(ResultUtils.success(chunk))
                        .build())
                .onErrorResume(e -> Flux.just(ServerSentEvent.<BaseResponse<String>>builder()
                        .event("error")
                        .data(ResultUtils.error(ErrorCode.SYSTEM_ERROR , e.getMessage()))
                        .build()));
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        // 获取 Flux 数据流并直接订阅
        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(ResultUtils.success(chunk));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 错误：发一条错误响应，再正常结束 
                        e -> { 
                            try {
                                emitter.send(ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage()));
                            } catch (IOException ignored) {
                            }
                            emitter.complete();
                        },
                        // 处理完成
                        emitter::complete
                );
        // 返回emitter
        return emitter;
    }


    /**
     * 流式调用selfManus
     * @param message 用户输入提示词
     * @return sseEmitter
     */
    @GetMapping("/self_manus/chat")
    public SseEmitter doChatWithSelfManus(String message){
        SelfManus selfManus = new SelfManus(allTools, dashscopeChatModel);
        return selfManus.runStream(message);
    }



}
