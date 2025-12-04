package com.n1etzsch3.novi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
		com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration.class,
		com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeImageAutoConfiguration.class,
		com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioTranscriptionAutoConfiguration.class,
		com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioSpeechAutoConfiguration.class,
		com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAgentAutoConfiguration.class,
		com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeVideoAutoConfiguration.class,
		com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeRerankAutoConfiguration.class,
		com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeEmbeddingAutoConfiguration.class
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
