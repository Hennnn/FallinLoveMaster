package com.chenj.cjaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String conversationId = UUID.randomUUID().toString();
        //第一轮
        String message = "你好，我是擎天柱";
        String answer = loveApp.doChat(message,conversationId);
        Assertions.assertNotNull(answer);
        //第二轮
        message = "我想和大黄蜂交往，你有什么建议";
        answer = loveApp.doChat(message,conversationId);
        Assertions.assertNotNull(answer);
        //第三轮
        message = "我想交往的对象是谁来着？我刚刚跟你说过，帮我回忆以下";
        answer = loveApp.doChat(message,conversationId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String conversationId = UUID.randomUUID().toString();
        String message = "你好，我是程序员小米，我想让另一半（编程导航）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, conversationId);
        Assertions.assertNotNull(loveReport);

    }

    @Test
    void doChatWithRag() {
        String conversationId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, conversationId);
        Assertions.assertNotNull(answer);
    }
}