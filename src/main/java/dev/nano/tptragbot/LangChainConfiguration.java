package dev.nano.tptragbot;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LangChainConfiguration {

    @Value("${langchain.api.key}")
    private String apiKey;

    @Value("${langchain.timeout}")
    private Long timeout;

    @Bean
    public ConversationalRetrievalChain chain(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        log.info("Creating ConversationalRetrievalChain bean");
        return ConversationalRetrievalChain.builder()
                .chatLanguageModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .timeout(Duration.ofSeconds(timeout))
                        .build()
                )
                //.promptTemplate(PromptTemplate.from(PROMPT_TEMPLATE))
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Creating EmbeddingModel bean");
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("Creating EmbeddingStore bean");
        return new InMemoryEmbeddingStore<>();
    }
}
