package com.chenj.cjaiagent.demo.invoke;// 建议dashscope SDK的版本 >= 2.12.0
import java.util.Arrays;
import java.lang.System;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;


/**
 * 阿里云灵积  SDK 调用 AI
 */
public class SdkAiInvoke {
    public static GenerationResult callWithMessage() throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())//构建ai的系统预设（人设）
                .content("You are a helpful assistant.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("你好，我是大帅哥，你认同吗？")//我问ai的内容
                .build();
        GenerationParam param = GenerationParam.builder()//定义一些生成的配置
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(TestApiKey.API_KEY) //替换为你自己的apikey
                // 此处以qwen-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")//选哪个大模型？
                .messages(Arrays.asList(systemMsg, userMsg))//生成结果
                .resultFormat(GenerationParam.ResultFormat.MESSAGE) //再格式化结果
                .build();
        return gen.call(param);
    }
    public static void main(String[] args) {
        try {
            GenerationResult result = callWithMessage(); //调用ai返回给result
            System.out.println(JsonUtils.toJson(result)); //再将结果转为json格式
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            // 使用日志框架记录异常信息
            System.err.println("An error occurred while calling the generation service: " + e.getMessage());
        }
        System.exit(0);
    }
}