package com.chenj.cjimagesearchmcpserver;

import com.chenj.cjimagesearchmcpserver.tools.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CjImageSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CjImageSearchMcpServerApplication.class, args);
    }

    @Bean //spring项目启动时，会自动读取我们的工具，然后生成一个工具提供者，然后客户端就可以使用这个提供者
    public ToolCallbackProvider imageSearchTools(ImageSearchTool imageSearchTool){
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool) //推荐一个工具开一个项目
                .build();
    }
}
