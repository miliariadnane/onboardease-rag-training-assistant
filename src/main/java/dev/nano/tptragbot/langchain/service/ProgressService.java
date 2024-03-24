package dev.nano.tptragbot.langchain.service;

import dev.nano.tptragbot.common.model.Progress;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProgressService {

    private final Map<String, Progress> progressMap = new ConcurrentHashMap<>();

    public Progress getProgress(String sessionId) {
        return progressMap.computeIfAbsent(sessionId, k -> new Progress());
    }

    public void clearProgress(String sessionId) {
        progressMap.remove(sessionId);
    }
}
