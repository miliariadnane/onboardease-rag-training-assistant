package dev.nano.tptragbot.springai.service;

import dev.nano.tptragbot.springai.configuration.OpenAIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.nano.tptragbot.common.Constant.PROMPT_TEMPLATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpringAiService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final OpenAIClient openAiChatClient;

    public String askLLM(String query) {

        List<Document> similarities = vectorStore.similaritySearch(query);
        String systemMessageTemplate = PROMPT_TEMPLATE + """
            CONTEXT:
              {CONTEXT}
            """;
        Message systemMessage = new SystemPromptTemplate(systemMessageTemplate)
                .createMessage(Map.of("CONTEXT", similarities));
        UserMessage userMessage = new UserMessage(query);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return openAiChatClient.getOpenAiChatClient().call(prompt).getResult().getOutput().getContent();
    }


    public void textEmbedding(Resource[] pdfResources) {
        jdbcTemplate.update("delete from vector_store");

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(
                        new ExtractedTextFormatter.Builder()
                                .withNumberOfBottomTextLinesToDelete(3)
                                .withNumberOfTopPagesToSkipBeforeDelete(1)
                                .build()
                )
                .withPagesPerDocument(1)
                .build();

        StringBuilder content = new StringBuilder();

        log.info("Reading PDFs...");
        for (Resource resource : pdfResources) {
            PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(resource, config);
            List<Document> documentList = pdfDocumentReader.get();
            content.append(documentList.stream().map(Document::getContent)
                    .collect(Collectors.joining("\n")))
                    .append("\n");
        }

        log.info("Splitting content into chunks...");
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<String> chunks = tokenTextSplitter.split(content.toString(), 1000);
        List<Document> chunksDocs = chunks.stream().map(Document::new).toList();

        log.info("Embedding chunks...");
        vectorStore.accept(chunksDocs);
    }
}
