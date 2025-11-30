package com.n1etzsch3.novi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
		org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration.class
})
@ComponentScan(basePackages = {
		"com.n1etzsch3.novi",
		"com.n1etzsch3.novi.common",
		"com.n1etzsch3.novi.user",
		"com.n1etzsch3.novi.chat",
		"com.n1etzsch3.novi.question",
		"com.n1etzsch3.novi.aiconfig"
})
@MapperScan(basePackages = {
		"com.n1etzsch3.novi.user.mapper",
		"com.n1etzsch3.novi.chat.mapper",
		"com.n1etzsch3.novi.question.mapper",
		"com.n1etzsch3.novi.aiconfig.mapper"
})
public class NoviApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoviApplication.class, args);
	}

}
