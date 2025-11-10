package com.n1etzsch3.novi.TestAIAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 讯飞星火 (OpenAI 兼容) API 接口调用 Demo
 * 适用于: <a href="https://spark-api-open.xf-yun.com/v1/chat/completions">...</a>
 * 文档: <a href="https://www.xfyun.cn/doc/spark/HTTP%E8%B0%83%E7%94%A8%E6%96%87%E6%A1%A3.html">...</a>
 *
 * 依赖: okhttp3, jackson-databind, lombok
 */
public class SparkOpenApiDemo {

    // --- 配置 APIPassword ---
    //
    private static final String API_PASSWORD = "bWKFlDkttpuUmyZmtHqw:MhoHyByQfVuYCCuxfIjL";

    // --- API 访问地址 ---
    private static final String API_URL = "https://spark-api-open.xf-yun.com/v1/chat/completions";

    // --- 指定要使用的模型版本 ---
    // 根据文档 3.3 节，例如: "generalv3.5" (Max), "4.0Ultra", "pro-128k", "lite"
    private static final String MODEL_VERSION = "lite";

    // --- 4. 核心组件 ---
    private final OkHttpClient httpClient = new OkHttpClient.Builder().build();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * 主测试方法
     */
    public static void main(String[] args) {

        try {
            SparkOpenApiDemo demo = new SparkOpenApiDemo();

            // 构造提问内容 (OpenAI 格式)
            List<OpenAIMessage> messages = new ArrayList<>();
            Scanner scanner = new Scanner(System.in);
            System.out.println("请输入您的问题 (输入 'exit' 退出):");
            String input = scanner.nextLine();
            messages.add(OpenAIMessage.builder().role("user").content(input).build());

            // 构建 JSON
            String jsonRequest = demo.buildJsonRequest(messages);

            System.out.println("--- 请求 URL ---");
            System.out.println(API_URL);
            System.out.println("\n--- 请求 Model ---");
            System.out.println(MODEL_VERSION);
            System.out.println("\n--- 请求 Body (JSON) ---");
            System.out.println(jsonRequest);
            System.out.println("\n--- Authorization Header ---");
            System.out.println("Bearer " + (API_PASSWORD.length() > 4 ? API_PASSWORD.substring(0, 4) : "") + "...");


            // 发送 POST 请求
            String jsonResponse = demo.postHttpRequest(jsonRequest);

            System.out.println("\n--- 原始 JSON 响应 ---");
            System.out.println(jsonResponse);

            // 解析响应
            String answer = demo.parseJsonResponse(jsonResponse);
            System.out.println("\n--- AI 的回复 ---");
            System.out.println(answer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送 HTTP POST 请求
     * 使用 Bearer Token 鉴权
     */
    private String postHttpRequest(String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                // (关键) 根据文档 3.2 节，添加鉴权头
                .addHeader("Authorization", "Bearer " + API_PASSWORD)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            // 无论成功失败，都读取 body
            String responseBodyString = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                // 如果失败，抛出包含响应体的异常
                throw new IOException("请求失败: " + response.code() + " " + response.message() + "\n响应体: " + responseBodyString);
            }

            // 成功，返回响应体
            return responseBodyString;
        }
    }

    /**
     * 构建 OpenAI 格式的 JSON 请求体
     */
    private String buildJsonRequest(List<OpenAIMessage> messages) throws Exception {
        // 使用 OpenAI 格式
        OpenAIRequest request = OpenAIRequest.builder()
                .model(MODEL_VERSION) // 使用指定的模型版本
                .messages(messages)
                .temperature(0.5)   // (可选)
                .maxTokens(4096)    // (可选)
                .build();

        return jsonMapper.writeValueAsString(request);
    }

    /**
     * 解析 OpenAI 格式的 JSON 响应体
     */
    private String parseJsonResponse(String jsonResponse) throws Exception {
        // 使用 OpenAI 格式的 POJO
        OpenAIResponse response = jsonMapper.readValue(jsonResponse, OpenAIResponse.class);

        // 检查是否有业务错误
        if (response.error != null) {
            throw new RuntimeException("API 调用失败: " + response.error.message + " (Type: " + response.error.type + ", Code: " + response.error.code + ")");
        }

        // 提取内容
        if (response.choices != null && !response.choices.isEmpty()) {
            OpenAIMessage message = response.choices.get(0).message;
            if (message != null && message.content != null) {
                return message.content;
            }
        }

        // 检查响应中是否包含 code (另一种错误格式)
        if (response.code != null && response.code != 0) {
            throw new RuntimeException("API 调用失败: " + response.message + " (Code: " + response.code + ")");
        }

        throw new RuntimeException("未能在响应中找到 AI 回复");
    }
}

// --- POJO - 用于 OpenAI 格式 ---

// (请求体 POJO)
@Data
@Builder
class OpenAIRequest {
    String model;
    List<OpenAIMessage> messages;

    @Builder.Default
    Double temperature = 0.5;

    @Builder.Default
    @JsonProperty("max_tokens")
    Integer maxTokens = 4096;
}

// (通用消息体)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class OpenAIMessage {
    String role;
    String content;
}

// (响应体 POJO)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class OpenAIResponse {
    List<Choice> choices;
    Usage usage;
    Error error;
    Integer code;
    String message;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 加在这里
    static class Choice {
        OpenAIMessage message;
        int index; // 可选
    }

    @Data
    static class Usage {
        @JsonProperty("prompt_tokens")
        int promptTokens;
        @JsonProperty("completion_tokens")
        int completionTokens;
        @JsonProperty("total_tokens")
        int totalTokens;
    }

    @Data
    static class Error {
        String message;
        String type;
        Object code;
    }
}
