package dev.nano.tptragbot.langchain.service;

import org.springframework.stereotype.Service;

import dev.nano.tptragbot.langchain.agent.OnboardTrainingAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TPTBotService {

    private final OnboardTrainingAssistant chain;

    public String askQuestion(String question) {
        log.debug("======================================================");
        log.debug("Question: " + question);
        String answer = chain.chat(question);
        log.debug("Answer: " + answer);
        log.debug("======================================================");
        return answer;
    }
}
