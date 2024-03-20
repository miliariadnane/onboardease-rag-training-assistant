package dev.nano.tptragbot.langchain.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Setter
@Getter
@Service
public class ApiKeyHolderService {
    private String apiKey;
}
