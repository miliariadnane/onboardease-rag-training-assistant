package dev.nano.tptragbot.langchain.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.nano.tptragbot.langchain.model.Progress;
import dev.nano.tptragbot.langchain.configuration.DocumentConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DocumentConfiguration documentConfiguration;
    private final Tokenizer tokenizer;

    public void ingestDocuments(List<String> urls, List<String> paths, Progress progress) {
        log.info("Starting document ingestion");

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(
                        DocumentSplitters.recursive(500, 100, tokenizer))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        List<Document> documents = documentConfiguration.documents(urls, paths);

        int totalDocuments = documents.size();
        log.info("Total documents: {}", totalDocuments);
        progress.setTotal(totalDocuments);

        for (Document document : documents) {
            ingestor.ingest(Collections.singletonList(document));
            progress.increment();
            log.info("Progress: {}", progress.getPercentage());
        }

        log.info("Document ingestion completed");
    }
}
