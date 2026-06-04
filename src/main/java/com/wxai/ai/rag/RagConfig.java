package com.wxai.ai.rag;


import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Configuration
public class RagConfig {

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Value("${rag.docs-path:classpath:docs/}")
    private String docsPath;

    @Value("${rag.max-results:5}")
    private int maxResults;

    @Value("${rag.min-score:0.7}")
    private double minScore;

    @Value("${rag.enabled:false}")
    private boolean enabled;

    @Bean
    public ContentRetriever contentRetriever(){
        if (!enabled) {
            log.info("RAG 功能已关闭（rag.enabled=false），跳过文档加载，不消耗 Embedding Token");
            return EmbeddingStoreContentRetriever.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .build();
        }
        List<Document> documents = loadDocuments();
        if (documents.isEmpty()) {
            log.warn("RAG 文档目录为空，跳过 ingest");
        } else {
            DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(400, 50);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(paragraphSplitter)
                    .textSegmentTransformer(textSegment -> textSegment.from(
                                    textSegment.metadata().getString("file_name")
                                            + "\n" + textSegment.text(), textSegment.metadata()
                            )
                    )
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            ingestor.ingest(documents);
            log.info("RAG 文档加载完成，共 {} 篇", documents.size());
        }

        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }

    private List<Document> loadDocuments() {
        List<Document> docs = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            org.springframework.core.io.Resource[] resources = resolver.getResources(docsPath + "*.md");
            for (org.springframework.core.io.Resource resource : resources) {
                try {
                    String text = new String(resource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    String fileName = resource.getFilename();
                    docs.add(Document.from(text, dev.langchain4j.data.document.Metadata.from("file_name", fileName != null ? fileName : "unknown")));
                } catch (IOException e) {
                    log.error("加载文档失败: {}", resource.getFilename(), e);
                }
            }
        } catch (IOException e) {
            log.error("扫描文档目录失败: {}", docsPath, e);
        }
        return docs;
    }
}