package com.n1etzsch3.novi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Novi 纯 Java 命令行客户端
 * 功能：登录、流式对话、上下文记忆演示
 */
public class NoviConsoleClient {

    private static final String BASE_URL = "http://localhost:8080/api/v1";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS) // 流式对话需要较长的超时时间
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String jwtToken = null;
    private static String currentSessionId = null;

    // ANSI 颜色码，让控制台输出不枯燥
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";   // 用户
    private static final String ANSI_GREEN = "\u001B[32m";  // AI
    private static final String ANSI_YELLOW = "\u001B[33m"; // 系统提示

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(ANSI_YELLOW + "==========================================");
        System.out.println("       Novi AI 终端客户端 v1.0");
        System.out.println("==========================================" + ANSI_RESET);

        // 1. 登录流程
        while (jwtToken == null) {
            System.out.println("\n请选择: 1. 登录  2. 注册 (输入 1 或 2)");
            String choice = scanner.nextLine();
            if ("2".equals(choice)) {
                doRegister(scanner);
            } else {
                doLogin(scanner);
            }
        }

        System.out.println(ANSI_YELLOW + "\n>>> 登录成功！开启聊天模式 (输入 'exit' 退出, 'new' 开启新对话) <<<" + ANSI_RESET);

        // 2. 聊天循环
        while (true) {
            System.out.print(ANSI_BLUE + "\nUser: " + ANSI_RESET);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;
            if ("exit".equalsIgnoreCase(input)) break;
            if ("new".equalsIgnoreCase(input)) {
                currentSessionId = null;
                System.out.println(ANSI_YELLOW + "[已切换到新会话]" + ANSI_RESET);
                continue;
            }

            // 发送流式请求
            chatStream(input);
        }
    }

    private static void doLogin(Scanner scanner) {
        System.out.print("用户名: ");
        String username = scanner.nextLine();
        System.out.print("密  码: ");
        String password = scanner.nextLine();

        try {
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            String response = post("/users/login", json);
            JsonNode node = mapper.readTree(response);
            if (node.get("code").asInt() == 1) {
                jwtToken = node.get("data").get("token").asText();
                System.out.println(ANSI_GREEN + "✔ 登录成功" + ANSI_RESET);
            } else {
                System.err.println("❌ " + node.get("msg").asText());
            }
        } catch (Exception e) {
            System.err.println("❌ 网络错误: " + e.getMessage());
        }
    }

    private static void doRegister(Scanner scanner) {
        System.out.println("--- 快速注册 ---");
        System.out.print("用户名: "); String u = scanner.nextLine();
        System.out.print("昵  称: "); String n = scanner.nextLine();
        System.out.print("邮  箱: "); String e = scanner.nextLine();
        System.out.print("密  码: "); String p = scanner.nextLine();
        // 省略了复杂的 JSON 构建，直接拼字符串演示
        String json = String.format("{\"username\":\"%s\",\"nickname\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", u, n, e, p);
        try {
            String res = post("/users/register", json);
            System.out.println("结果: " + res);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // 核心：流式对话处理
    private static void chatStream(String message) {
        System.out.print(ANSI_GREEN + "Novi: " + ANSI_RESET); // AI 前缀

        // 构建请求 JSON
        String jsonBody = String.format("{\"message\":\"%s\", \"sessionId\": %s}",
                message,
                currentSessionId == null ? "null" : "\"" + currentSessionId + "\"");

        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/send/stream") // 调用流式接口
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        // 使用 execute() 并获取流
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Error: " + response.code());
                return;
            }

            // 读取 SSE 流
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // SSE 格式通常是 "data: {...}"
                if (line.startsWith("data:")) {
                    String data = line.substring(5).trim(); // 去掉 "data:"
                    try {
                        JsonNode event = mapper.readTree(data);
                        String type = event.get("eventType").asText();

                        if ("CONTENT".equals(type)) {
                            // 打印内容（不换行），实现打字机效果
                            String content = event.get("content").asText();
                            System.out.print(ANSI_GREEN + content + ANSI_RESET);
                        } else if ("METADATA".equals(type)) {
                            // 更新会话ID
                            if (event.has("sessionId")) {
                                currentSessionId = event.get("sessionId").asText();
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
            System.out.println(); // 对话结束换行

        } catch (IOException e) {
            System.err.println("流读取中断: " + e.getMessage());
        }
    }

    // 普通 POST 工具方法
    private static String post(String endpoint, String json) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}