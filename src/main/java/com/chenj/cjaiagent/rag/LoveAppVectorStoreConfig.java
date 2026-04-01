package com.chenj.cjaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 恋爱大师向量数据库（初始化基于内存的向量数据库 BEAN)
 */
@Component
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        //加载文档
        List<Document> documentList = loveAppDocumentLoader.loadMarkdowns();
        //元信息增强器:自动为文档补充元信息
        List<Document> enrichDocument = myKeywordEnricher.enrichDocument(documentList);
        simpleVectorStore.doAdd(enrichDocument);
        return simpleVectorStore;
    }
}
