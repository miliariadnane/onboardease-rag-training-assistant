package dev.nano.tptragbot.springai.controller;

import dev.nano.tptragbot.common.model.Progress;
import dev.nano.tptragbot.common.util.filemanagement.FileManager;
import dev.nano.tptragbot.langchain.service.ProgressService;
import dev.nano.tptragbot.springai.service.SpringAIService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/spring-ai")
@RequiredArgsConstructor
@Slf4j
public class SpringAIController {

    private final FileManager filemanager;
    private final SpringAIService springAiService;
    private final ProgressService progressService;

    // This map will store the uploaded documents for each session
    private final Map<String, List<String>> documentMap = new ConcurrentHashMap<>();

    @GetMapping("/progress")
    public ResponseEntity<Integer> getProgress(HttpServletRequest request) {
        Progress progress = progressService.getProgress(request.getSession().getId());
        return ResponseEntity.ok(progress.getPercentage());
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam("files") MultipartFile[] files,
            HttpServletRequest request
    ) {
        log.info("Received upload request for Spring AI with {} files", files.length);

        if (files.length == 0) {
            return ResponseEntity.badRequest().body("No file selected, please select a file to upload.");
        }

        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String path = filemanager.storeFile(file);
                paths.add(path);
                log.info("Stored file at path: {}", path);
            }
        }

        // Store the paths for later ingestion
        documentMap.put(request.getSession().getId(), paths);
        log.info("Stored paths for session: {}", request.getSession().getId());

        return ResponseEntity.ok("Files uploaded successfully");
    }

    @PostMapping("/ingest")
    public String ingest(HttpServletRequest request, Model model) {
        log.info("Received ingest request for Spring AI");

        List<String> paths = documentMap.get(request.getSession().getId());
        log.info("Retrieved paths for session: {}", request.getSession().getId());

        if (paths == null || paths.isEmpty()) {
            model.addAttribute("errorMessage", "No documents to ingest, please upload some files first.");
            return "index";
        }

        log.info("paths: {}", paths);
        Resource[] resources = paths.stream()
                .map(FileSystemResource::new)
                .toArray(Resource[]::new);

        Progress progress = progressService.getProgress(request.getSession().getId());
        springAiService.textEmbedding(resources, progress);

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
            return ResponseEntity.ok(springAiService.askLLM(question));
        } catch (Exception e) {
            log.error("Failed to process question", e);
            return ResponseEntity.badRequest().body("Sorry, I can't process your question right now.");
        }
    }
}
