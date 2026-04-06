package com.chenj.cjimagesearchmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void searchImage(){
        String image = imageSearchTool.searchImage("compute");
        Assertions.assertNotNull(image); //工具开发好了，怎么注册起来？

    }
}
