package dev.nano.tptragbot.langchain.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import dev.langchain4j.data.document.Document;
import static dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocument;
import dev.langchain4j.data.document.UrlDocumentLoader;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class DocumentConfiguration {

    public List<Document> documents(List<String> urls, List<String> paths) {

        List<Document> documents = new ArrayList<>();

        // Load documents from URLs
        for (String url : urls) {
            try {
                documents.add(UrlDocumentLoader.load(url));
                //log.info("Loaded document from URL: {}", url);
            } catch (Exception e) {
                log.error("Failed to load document from URL: {}", url, e);
                throw new RuntimeException("Failed to load document from " + url, e);
            }
        }

        // Load documents from PDF files
        for (String path : paths) {
            try {
                documents.add(loadDocument(path));
                //log.info("Loaded document from path: {}", path);
            } catch (Exception e) {
                log.error("Failed to load document from path: {}", path, e);
                throw new RuntimeException("Failed to load document from " + path, e);
            }
        }

        log.info("Total documents loaded: {}", documents.size());
        return documents;
    }
}
