package com.chenj.cjaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;


public class LangChainAiInvoke {

    public static void main(String[] args) {
        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String answer = qwenChatModel.chat("我是大四学生，怎么学习编程？");
        System.out.println(answer);
    }
}
