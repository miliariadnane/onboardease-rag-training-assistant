package dev.nano.tptragbot.springai.controller;

import dev.nano.tptragbot.langchain.service.FileStorageService;
import dev.nano.tptragbot.springai.service.SpringAiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import dev.nano.tptragbot.common.DocumentSources;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
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
public class SpringAiController {

    private final FileStorageService fileStorageService;
    private final SpringAiService springAiService;

    // This map will store the uploaded documents for each session
    private final Map<String, List<String>> documentMap = new ConcurrentHashMap<>();

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
                String path = fileStorageService.storeFile(file);
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
    public ResponseEntity<String> ingest(HttpServletRequest request) {
        log.info("Received ingest request for Spring AI");

        List<String> paths = documentMap.get(request.getSession().getId());
        log.info("Retrieved paths for session: {}", request.getSession().getId());

        if (paths == null || paths.isEmpty()) {
            return ResponseEntity.badRequest().body("No documents to ingest, please upload some files first.");
        }

        Resource[] resources = paths.stream()
                .map(FileSystemResource::new)
                .toArray(Resource[]::new);

        springAiService.textEmbedding(resources);

        return ResponseEntity.ok("Documents ingested successfully for Spring AI");
    }
}
