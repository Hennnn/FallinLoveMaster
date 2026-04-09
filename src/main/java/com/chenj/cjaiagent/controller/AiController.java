package com.chenj.cjaiagent.controller;

import com.chenj.cjaiagent.agent.CjManus;
import com.chenj.cjaiagent.app.LoveApp;
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

    /**
     * 同步调用AI情感大师应用
     * @param message
     * @param conversationId
     * @return
     */
    @GetMapping("/love_app/chat/sync") //同步
    public String daChatWithLoveAppSync(String message,String conversationId ){
        return loveApp.doChat(message, conversationId);
    }
    /**
     * 流式调用AI情感大师应用 （三种异步）
     * @param message
     * @param conversationId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> daChatWithLoveAppSSE(String message, String conversationId) {
        return loveApp.doChatByStream(message, conversationId);
    }
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> daChatWithLoveAppServerSentEvent(String message, String conversationId) {
        return loveApp.doChatByStream(message, conversationId)
                .map(chunk ->ServerSentEvent.<String>builder().data(chunk).build()
                );
    }
    @GetMapping("/love_app/chat/sse/emitter") //异步中最灵活可控的一种
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);
        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,

                        emitter::complete
                );
        return emitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message){
        CjManus cjManus = new CjManus(allTools,dashscopeChatModel);
        SseEmitter sseEmitter = cjManus.runStream(message);
        return sseEmitter;
    }

}
