package com.chenj.cjaiagent.rag;


import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 自定义基于阿里云知识库服务的RAG增强顾问
 */
@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;
    @Value("${spring.ai.dashscope.chat.rag.index-name}")
    private String knowledgeBaseName;

    @Bean
    public Advisor loveAppRagCloudAdvisor() {

        try {
            // 使用 Builder 模式创建 DashScopeApi，而不是手动 new 构造函数
            DashScopeApi dashScopeApi = DashScopeApi.builder()
                    .apiKey(dashscopeApiKey) // 设置 API Key
                    // 如果需要在Header 传递 WorkspaceId，可以在这里添加 .workspaceId(...)
                    .build();
            DashScopeDocumentRetrieverOptions options = DashScopeDocumentRetrieverOptions.builder()
                    .indexName(knowledgeBaseName) // 必须是云端存在的名字
                    .denseSimilarityTopK(3)      // 返回 Top3 文档
                    .enableRewrite(true)         // 开启查询重写
                    .enableReranking(true)       // 开启重排序
                    .build();

            DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi, options);

            log.info("已初始化基于百炼知识库 ({}) 的 RAG 顾问", knowledgeBaseName);

            return RetrievalAugmentationAdvisor.builder() //还可以设置查询前用 文档重写器 .queryTransformers(RewriteQueryTransformer.builder()
                    .documentRetriever(documentRetriever) //查询过程中用 文档加载器
                    .build();
        } catch (Exception e) {
            log.error("初始化百炼 RAG 顾问失败", e);
            throw new RuntimeException("RAG 顾问初始化异常", e);
        }
    }
}
