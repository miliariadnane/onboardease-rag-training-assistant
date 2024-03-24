package dev.nano.tptragbot.springai.service;

import dev.nano.tptragbot.springai.configuration.OpenAIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
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

import static dev.nano.tptragbot.common.constant.Constant.PROMPT_TEMPLATE;
import static dev.nano.tptragbot.common.util.reader.FileReaderUtil.readCsvFile;
import static dev.nano.tptragbot.common.util.reader.FileReaderUtil.readExcelFile;

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


    public void textEmbedding(Resource[] resources) {
        jdbcTemplate.update("delete from vector_store");

        StringBuilder content = new StringBuilder();

        log.info("Reading files...");
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            String extension = FilenameUtils.getExtension(filename);

            List<Document> documentList;
            switch (extension) {
                case "pdf":
                    var pdfConfig = PdfDocumentReaderConfig.builder()
                            .withPageExtractedTextFormatter(
                                    new ExtractedTextFormatter.Builder()
                                            .withNumberOfBottomTextLinesToDelete(3)
                                            .withNumberOfTopPagesToSkipBeforeDelete(1)
                                            .build()
                            )
                            .withPagesPerDocument(1)
                            .build();
                    PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(resource, pdfConfig);
                    documentList = pdfDocumentReader.get();
                    break;
                case "txt":
                    TextReader textDocumentReader = new TextReader(resource);
                    documentList = textDocumentReader.get();
                    break;
                case "json":
                    JsonReader jsonDocumentReader = new JsonReader(resource);
                    documentList = jsonDocumentReader.get();
                    break;
                case "xls", "xlsx":
                    String text = readExcelFile(resource);
                    documentList = List.of(new Document(text));
                    break;
                case "csv":
                    String csvText = readCsvFile(resource);
                    documentList = List.of(new Document(csvText));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + extension);
            }

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
