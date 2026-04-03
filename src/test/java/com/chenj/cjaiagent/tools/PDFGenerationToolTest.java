package com.chenj.cjaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "编程原创项目.pdf";
        String content = "编程原创项目 https://www.wechat.cn";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}