package com.chenj.cjaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;


/**
 * 自定义日志 advisor
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    //private static final Logger logger = LoggerFactory.getLogger(MyLoggerAdvisor.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }


    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        logRequest(chatClientRequest); //先调用

        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

        logResponse(chatClientResponse);

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        logRequest(chatClientRequest);

        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);//因为采用流式响应，所以用个聚合器来处理
    }

    private void logRequest(ChatClientRequest request) {
        log.info("AI Request:{}",request.prompt().getUserMessage().getText()); //自定义打印调用前的usermessage文本
    }

    private void logResponse(ChatClientResponse chatClientResponse) {
        log.info("AI Response:{}",chatClientResponse.chatResponse().getResult().getOutput().getText());//自定义打印调用后返回的文本
    }

}
