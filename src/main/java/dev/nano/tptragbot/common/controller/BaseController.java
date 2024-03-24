package dev.nano.tptragbot.common.controller;

import dev.nano.tptragbot.common.model.Progress;
import dev.nano.tptragbot.langchain.service.ProgressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
public abstract class BaseController {

    private final ProgressService progressService;

    @GetMapping("/progress")
    public ResponseEntity<Integer> getProgress(HttpServletRequest request) {
        Progress progress = progressService.getProgress(request.getSession().getId());
        return ResponseEntity.ok(progress.getPercentage());
    }
}
