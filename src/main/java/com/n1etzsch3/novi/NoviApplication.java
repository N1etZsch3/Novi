package com.n1etzsch3.novi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration.class
})
public class NoviApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoviApplication.class, args);
	}

}
