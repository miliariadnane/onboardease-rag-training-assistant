package dev.nano.tptragbot.langchain.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.nano.tptragbot.common.model.DocumentSources;
import dev.nano.tptragbot.common.util.filemanagement.FileManager;
import dev.nano.tptragbot.common.model.Progress;
import dev.nano.tptragbot.langchain.service.TPTBotService;
import dev.nano.tptragbot.langchain.configuration.DocumentConfiguration;
import dev.nano.tptragbot.langchain.service.ApiKeyHolderService;
import dev.nano.tptragbot.langchain.service.DocumentIngestionService;
import dev.nano.tptragbot.langchain.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import dev.langchain4j.data.document.Document;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/langchain")
@RequiredArgsConstructor
@Slf4j
public class TPTBotController {

    private final DocumentConfiguration documentConfiguration;
    private final DocumentIngestionService documentIngestionService;
    private final FileManager fileManager;
    private final TPTBotService tptBotService;
    private final ProgressService progressService;
    private final ApiKeyHolderService apiKeyHolderService;


    // This map will store the uploaded documents for each session
    private final Map<String, DocumentSources> documentMap = new ConcurrentHashMap<>();

    @GetMapping
    public String home() {
        return "index";
    }

    @PostMapping("/api-key")
    public ResponseEntity<Void> setApiKey(@RequestBody String apiKey) {
        apiKeyHolderService.setApiKey(apiKey);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/upload")
    public String upload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("url") List<String> urls,
            HttpServletRequest request,
            Model model
    ) {
        log.info("Received upload request with {} files and URLs: {}", files.length, urls);

        if (files.length == 0) {
            model.addAttribute("errorMessage", "No file selected, please select a file to upload.");
            return "index";
        }
        if (urls == null || urls.isEmpty()) {
            model.addAttribute("errorMessage", "URL is empty, please enter a valid URL.");
            return "index";
        }

        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String path = fileManager.storeFile(file);
                paths.add(path);
                log.info("Stored file at path: {}", path);
            }
        }

        List<Document> documents = documentConfiguration.documents(urls, paths);
        log.info("Loaded {} documents", documents.size());

        // Store the documents for later ingestion
        documentMap.put(request.getSession().getId(), new DocumentSources(urls, paths));

        model.addAttribute("successMessage", "Files uploaded successfully");
        return "index";
    }

    @PostMapping("/ingest")
    public String ingest(HttpServletRequest request, Model model) {
        log.info("Received ingest request");

        // Retrieve the documents for ingestion
        DocumentSources documentSources = documentMap.get(request.getSession().getId());
        log.info("Retrieved URLs and paths for session: {}", request.getSession().getId());

        if (documentSources == null || documentSources.getUrls().isEmpty() && documentSources.getPaths().isEmpty()) {
            model.addAttribute("errorMessage", "No documents to ingest, please upload some files first.");
            return "index";
        }

        Progress progress = progressService.getProgress(request.getSession().getId());
        documentIngestionService.ingestDocuments(documentSources.getUrls(), documentSources.getPaths(), progress);

        model.addAttribute("progress", progress.getPercentage());
        return "index";
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        log.info("Received ask request with question: {}", question);

        if (question == null || question.isEmpty()) {
            return ResponseEntity.badRequest().body("Question is empty, please enter a valid question.");
        }

        try {
            return ResponseEntity.ok(tptBotService.askQuestion(question));
        } catch (Exception e) {
            log.error("Failed to process question", e);
            return ResponseEntity.badRequest().body("Sorry, I can't process your question right now.");
        }
    }
}
