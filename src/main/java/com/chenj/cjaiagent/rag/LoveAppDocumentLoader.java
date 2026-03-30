package com.chenj.cjaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 恋爱大师文档加载器
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;  //spring框架下的资源处理工具


    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) { //用于快速加载多篇文档
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇markdown文档
     * @return
     */
    public List<Document> loadMarkdowns(){
        List<Document> allDocuments = new ArrayList<>();
        //加载多篇markdown文档
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename(); //为文档取个“标签” ，后续也可能用到他来检索
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()   //定义一个markdown加载器
                        .withHorizontalRuleCreateDocument(true)  //可添加以下元信息（如下参数）指定具体文档细节
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        .build();//加载器设置完成
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config); //new一个资源获取器（就可以读入了）
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败",e);
        }
        return allDocuments;
    }

}
