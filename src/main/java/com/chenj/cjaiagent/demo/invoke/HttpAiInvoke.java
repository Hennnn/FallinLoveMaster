package com.chenj.cjaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;

/**
 * HTTP 请求方式调用AI
 */
public class HttpAiInvoke {
    public static void main(String[] args) {
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        String apiKey = TestApiKey.API_KEY; // 或从配置读取

        String json = """
                {
                    "model": "qwen-plus",
                    "input": {
                        "messages": [
                            {
                                "role": "system",
                                "content": "You are a helpful assistant."
                            },
                            {
                                "role": "user",
                                "content": "你是谁？"
                            }
                        ]
                    },
                    "parameters": {
                        "result_format": "message"
                    }
                }
                """;

        String response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(json)
                .timeout(60_000)
                .execute()
                .body();

        System.out.println(response);
    }
}
