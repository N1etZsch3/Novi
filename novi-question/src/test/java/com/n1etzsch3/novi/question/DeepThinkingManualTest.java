package com.n1etzsch3.novi.question;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public class DeepThinkingManualTest {

    // Replace with your actual API key or ensure it's set in environment variables
    private static final String API_KEY = "sk-dummy-key-for-testing";

    @Test
    public void testDeepThinkingStream() {
        System.out.println("Starting Deep Thinking Test...");
        System.out.println("API_KEY: " + (API_KEY != null ? "PRESENT" : "NULL"));

        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(API_KEY)
                .build();

        // Use DashScopeChatOptions
        DashScopeChatOptions options = com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions.builder()
                .withModel("qwen-max")
                .build();

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .observationRegistry(io.micrometer.observation.ObservationRegistry.NOOP)
                .build();

        // Do NOT set default options on the model
        // chatModel.setDashScopeChatOptions(options);

        Prompt prompt = new Prompt(new UserMessage("9.11 and 9.8, which is bigger?"), options);

        System.out.println("Prompt Options: " + prompt.getOptions());
        System.out.println("Model Default Options: " + chatModel.getDashScopeChatOptions());

        System.out.println("Sending request (call)...");
        try {
            System.out.println("Response (call): " + chatModel.call(prompt).getResult().getOutput().getText());
        } catch (Exception e) {
            System.out.println("Call failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Sending request (stream)...");

        try {
            Flux<ChatResponse> stream = chatModel.stream(prompt);

            stream.doOnNext(response -> {
                // Check for reasoning content in metadata
                if (response.getResults() != null && !response.getResults().isEmpty()) {
                    var metadata = response.getResults().get(0).getMetadata();
                    if (metadata != null) {
                        if (metadata.containsKey("reasoning_content")) {
                            System.out.print("[Thinking]: " + metadata.get("reasoning_content"));
                        } else if (metadata.containsKey("reasoningContent")) {
                            System.out.print("[Thinking]: " + metadata.get("reasoningContent"));
                        }
                    }

                    // Check for normal content
                    String content = response.getResults().get(0).getOutput().getText();
                    if (content != null) {
                        System.out.print(content);
                    }
                }
            }).blockLast();
        } catch (Exception e) {
            System.out.println("Stream failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\nTest Finished.");
    }
}
