package dev.nano.tptragbot.langchain.service;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.nano.tptragbot.langchain.agent.OnboardTrainingAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
