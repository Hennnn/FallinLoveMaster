package com.chenj.cjaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.client.advisor.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringAiInvoke implements CommandLineRunner {


    @Resource
    private ChatModel dashscopeChatModel;


    @Override
    public void run(String... args) throws Exception {
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你好你是机器人吗？"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
}
