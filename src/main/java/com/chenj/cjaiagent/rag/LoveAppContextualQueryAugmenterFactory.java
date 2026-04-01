package com.chenj.cjaiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 创建上下文查询增强器的 工厂
 */
public class LoveAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter creteInstance(){

        PromptTemplate emptyContextpromptTemplate = new PromptTemplate("""
        你应该输出下面的内容：
        抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
        有问题可以联系编程导航客服 https://codefather.cn
        
        query: {query}
        context: {context}
        """);
        return ContextualQueryAugmenter.builder()  //自定义：当ai检索不到，自己设置回复内容的 ContextualQueryAugment(上下文查询增强器）
                .allowEmptyContext(false) //若不想看到ai默认查不到的回复内容，则填FALSE，并在提示词模板中添加自己自定义回复内容“emptyContextpromptTemplate”
                .promptTemplate(emptyContextpromptTemplate)
                .build();
    }
}
