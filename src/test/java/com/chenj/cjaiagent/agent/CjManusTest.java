package com.chenj.cjaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CjManusTest {

    @Resource
    private CjManus cjManus;

    @Test
    public void run(){
        String userPrompt = """  
                我的另一半居住在上海静安区，请帮我搜索一些适合约会的地点，  
                并且直接下载一张适合做手机壁纸的星空情侣图片为文件，  
                并生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单""";
        String answer = cjManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }

}