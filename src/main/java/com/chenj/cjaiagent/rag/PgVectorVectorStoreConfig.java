package com.chenj.cjaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
@Slf4j
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel){

        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")  //表名
                .vectorTableName("vector_store")  //数据库名
                .maxDocumentBatchSize(50)  //单次最大插入可自定义实现
                .build();
        // 2. 加载所有文档
        List<Document> allDocuments = loveAppDocumentLoader.loadMarkdowns();

        log.info("开始向向量库添加文档，总数：{}", allDocuments.size());

        if (allDocuments.isEmpty()) {
            log.warn("未检测到任何文档，跳过入库操作");
            return vectorStore;
        }

        // 3. 【关键修复】分批添加，每次不超过 10 个（DashScope 限制）
        int batchSize = 10;
        for (int i = 0; i < allDocuments.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, allDocuments.size());
            List<Document> batch = allDocuments.subList(i, endIndex);

            log.debug("正在添加第 {} 批文档，数量：{}", (i/batchSize)+1, batch.size());

            // 执行添加，这里会触发 Embedding 调用
            vectorStore.add(batch);
        }

        log.info("向量库初始化完成，共添加了 {} 个文档分段", allDocuments.size());
//        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        vectorStore.add(documents);
        return vectorStore;
    }
}
