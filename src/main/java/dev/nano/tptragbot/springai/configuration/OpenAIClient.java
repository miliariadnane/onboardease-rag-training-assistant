package dev.nano.tptragbot.springai.configuration;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static dev.nano.tptragbot.common.constant.Constant.MODEL_NAME;

@Component
public final class OpenAIClient {

    @Value("${spring.ai.openai.api-key}")
    String apiKey;

    public OpenAiChatClient getOpenAiChatClient() {
        OpenAiApi openAiApi = new OpenAiApi(apiKey);
        OpenAiChatOptions options = new OpenAiChatOptions.Builder()
                .withModel(MODEL_NAME)
                .build();
        return new OpenAiChatClient(openAiApi, options);
    }
}
