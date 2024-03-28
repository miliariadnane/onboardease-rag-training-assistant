package dev.nano.tptragbot.langchain.configuration;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.nano.tptragbot.langchain.agent.OnboardTrainingAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static dev.nano.tptragbot.common.constant.Constant.MODEL_NAME;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class LangChainConfiguration {

    @Value("${langchain.timeout}")
    private Long timeout;

    @Value("${langchain.api-key}")
    private String apiKey;

    @Bean
    public OnboardTrainingAssistant chain(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            ChatMemory chatMemory
    ) {
        log.info("Creating OnboardTrainingAssistant Agent bean");
        return AiServices.builder(OnboardTrainingAssistant.class)
                .chatLanguageModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .timeout(Duration.ofSeconds(timeout))
                        .build()
                )
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                .chatMemory(chatMemory)
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

    // chatMemory is a bean that is used to store the chat history
    // It allows the chat to remember the context of the conversation which enhance conversation context understanding
    @Bean
    public MessageWindowChatMemory chatMemory() {
        log.info("Creating MessageWindowChatMemory bean");
        return MessageWindowChatMemory.withMaxMessages(20);
    }

    @Bean
    Tokenizer tokenizer() {
        return new OpenAiTokenizer(MODEL_NAME);
    }


    /*@Bean
    public ConversationalRetrievalChain chain(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            ChatMemory chatMemory
    ) {
        log.info("Creating ConversationalRetrievalChain bean");
        return ConversationalRetrievalChain.builder()
                .chatLanguageModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .timeout(Duration.ofSeconds(timeout))
                        .build()
                )
                //.promptTemplate(PromptTemplate.from(PROMPT_TEMPLATE))
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                .chatMemory(chatMemory)
                .build();
    }*/
}
