package com.n1etzsch3.novi.aiconfig.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Advisor to extract and process DashScope/DeepSeek reasoning content.
 */
public class ReasoningContentAdvisor implements CallAroundAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ReasoningContentAdvisor.class);

    @Override
    public String getName() {
        return "ReasoningContentAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        AdvisedResponse response = chain.nextAroundCall(advisedRequest);
        ChatResponse chatResponse = response.response();

        if (chatResponse == null || chatResponse.getResults() == null || chatResponse.getResults().isEmpty()) {
            return response;
        }

        Generation generation = chatResponse.getResults().get(0);
        ChatGenerationMetadata metadata = generation.getMetadata();

        // Extract reasoning content from metadata
        String reasoningContent = null;
        if (metadata != null) {
            if (metadata.containsKey("reasoning_content")) {
                reasoningContent = (String) metadata.get("reasoning_content");
            } else if (metadata.containsKey("reasoningContent")) {
                reasoningContent = (String) metadata.get("reasoningContent");
            }
        }

        if (StringUtils.hasText(reasoningContent)) {
            logger.info("Extracted reasoning content: {}", reasoningContent);

            // Reconstruct the content with reasoning
            AssistantMessage originalMessage = generation.getOutput();
            String originalContent = originalMessage.getText();

            String newContent = String.format("<think>\n%s\n</think>\n%s", reasoningContent, originalContent);

            // Create new AssistantMessage with modified content
            AssistantMessage newMessage = new AssistantMessage(newContent, originalMessage.getMetadata(),
                    originalMessage.getToolCalls(), originalMessage.getMedia());

            // Create new Generation with new message
            Generation newGeneration = new Generation(newMessage, generation.getMetadata());

            // Create new ChatResponse
            ChatResponse newChatResponse = new ChatResponse(Collections.singletonList(newGeneration),
                    chatResponse.getMetadata());

            return new AdvisedResponse(newChatResponse, response.adviseContext());
        }

        return response;
    }
}
