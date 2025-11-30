package com.n1etzsch3.novi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Novi AI - 完善版 Swing GUI 客户端
 * 功能：登录/注册、会话管理、流式对话、气泡渲染
 * * 优化日志：
 * 1. 添加 macOS 系统属性配置，优化输入法集成，减少 TSM/IMK 报错。
 * 2. 引入 ExecutorService 线程池管理网络请求，避免频繁创建线程。
 */
public class NoviGuiClient extends JFrame {

    // --- 配置 ---
    private static final String API_BASE = "http://localhost:8080/api/v1";
    // 颜色定义 (参考你的 Web版 CSS)
    private static final Color COLOR_PRIMARY = new Color(78, 110, 242); // #4e6ef2
    private static final Color COLOR_BG_USER = new Color(149, 236, 105); // 微信绿
    private static final Color COLOR_BG_AI = Color.WHITE;
    private static final Color COLOR_BG_MAIN = new Color(245, 245, 245);

    // --- 全局状态 ---
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.SECONDS) // 长超时以支持流式
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    // 【优化】使用缓存线程池管理并发请求，避免频繁 new Thread 造成的资源开销和潜在的系统调用冲突
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private String jwtToken = null;
    private String currentSessionId = null;
    private String currentUserNick = "User";

    // --- UI 组件 ---
    private CardLayout mainLayout;
    private JPanel mainPanel;
    private JPanel chatListPanel; // 会话列表容器
    private JPanel chatBoxPanel;  // 消息气泡容器
    private JScrollPane chatScrollPane;
    private JTextField inputField;
    private JLabel titleLabel;

    public static void main(String[] args) {
        // 【优化】macOS 专属兼容性设置
        // 解决 TSM AdjustCapsLockLED... 和 IMKCFRunLoopWakeUp... 报错的关键
        // 将菜单栏移动到屏幕顶部，减少 Swing 组件与原生输入法服务的冲突
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Novi");

        // 字体抗锯齿设置
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new NoviGuiClient().setVisible(true));
    }

    public NoviGuiClient() {
        setTitle("Novi · AI 挚友 (Java Client)");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainLayout = new CardLayout();
        mainPanel = new JPanel(mainLayout);
        add(mainPanel);

        // 初始化两个主要视图
        initAuthView();
        initChatView();

        // 默认显示登录页
        mainLayout.show(mainPanel, "AUTH");
    }

    // ==================== 1. 认证视图 (登录/注册) ====================

    private void initAuthView() {
        JPanel authPanel = new JPanel(new GridBagLayout());
        authPanel.setBackground(Color.WHITE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel title = new JLabel("Novi AI");
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 28));
        title.setForeground(COLOR_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        styleField(userField, "用户名");
        styleField(passField, "密码");

        JButton loginBtn = new JButton("登 录");
        styleButton(loginBtn, COLOR_PRIMARY);
        // 回车也可以触发登录
        ActionListener loginAction = e -> performLogin(userField.getText(), new String(passField.getPassword()));
        loginBtn.addActionListener(loginAction);
        passField.addActionListener(loginAction);

        JButton regBtn = new JButton("注册账号");
        regBtn.setBorderPainted(false);
        regBtn.setContentAreaFilled(false);
        regBtn.setForeground(Color.GRAY);
        regBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        regBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "请使用 Web 端或 Postman 注册账号 (CLI 模式支持注册命令行)。"));

        card.add(title);
        card.add(Box.createVerticalStrut(30));
        card.add(userField);
        card.add(Box.createVerticalStrut(15));
        card.add(passField);
        card.add(Box.createVerticalStrut(25));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(regBtn);

        authPanel.add(card);
        mainPanel.add(authPanel, "AUTH");
    }

    private void performLogin(String username, String password) {
        // 【优化】使用线程池提交任务
        executor.submit(() -> {
            try {
                // 构建登录请求
                String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
                String resp = post("/users/login", json);
                JsonNode node = mapper.readTree(resp);

                if (node.get("code").asInt() == 1) {
                    jwtToken = node.get("data").get("token").asText();
                    SwingUtilities.invokeLater(() -> {
                        mainLayout.show(mainPanel, "CHAT");
                        loadUserProfile();
                        loadSessions();
                    });
                } else {
                    showError(node.get("msg").asText());
                }
            } catch (Exception e) {
                showError("登录失败: " + e.getMessage());
            }
        });
    }

    // ==================== 2. 聊天主视图 ====================

    private void initChatView() {
        JPanel chatView = new JPanel(new BorderLayout());

        // --- 左侧侧边栏 ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // 侧边栏头部
        JPanel sideHeader = new JPanel(new BorderLayout());
        sideHeader.setBackground(Color.WHITE);
        sideHeader.setBorder(new EmptyBorder(15, 15, 15, 15));
        JButton newChatBtn = new JButton("+ 新建会话");
        styleButton(newChatBtn, COLOR_PRIMARY);
        newChatBtn.addActionListener(e -> startNewChat());
        sideHeader.add(newChatBtn, BorderLayout.CENTER);

        // 会话列表
        chatListPanel = new JPanel();
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatListPanel.setBackground(Color.WHITE);
        JScrollPane sideScroll = new JScrollPane(chatListPanel);
        sideScroll.setBorder(null);

        sidebar.add(sideHeader, BorderLayout.NORTH);
        sidebar.add(sideScroll, BorderLayout.CENTER);

        // --- 右侧聊天区 ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(COLOR_BG_MAIN);

        // 标题栏
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        header.setBackground(new Color(250, 250, 250));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        titleLabel = new JLabel("新会话");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        header.add(titleLabel);

        // 消息展示区 (支持滚动)
        chatBoxPanel = new JPanel();
        chatBoxPanel.setLayout(new BoxLayout(chatBoxPanel, BoxLayout.Y_AXIS));
        chatBoxPanel.setBackground(COLOR_BG_MAIN);
        chatBoxPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 关键：为了让 BoxLayout 正常工作，外层最好套一个 JPanel 并使用 BorderLayout.NORTH
        // 这样消息会从上往下排，不会挤在中间
        JPanel chatBoxWrapper = new JPanel(new BorderLayout());
        chatBoxWrapper.setBackground(COLOR_BG_MAIN);
        chatBoxWrapper.add(chatBoxPanel, BorderLayout.NORTH);

        chatScrollPane = new JScrollPane(chatBoxWrapper);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 输入区
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        inputField = new JTextField();
        inputField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        inputField.addActionListener(e -> sendMessage()); // 回车发送

        JButton sendBtn = new JButton("发送");
        styleButton(sendBtn, COLOR_PRIMARY);
        sendBtn.addActionListener(e -> sendMessage());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        rightPanel.add(header, BorderLayout.NORTH);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        // 组装 SplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, rightPanel);
        splitPane.setDividerSize(1);
        splitPane.setDividerLocation(260);

        chatView.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(chatView, "CHAT");
    }

    // ==================== 3. 业务逻辑 ====================

    private void loadUserProfile() {
        // 【优化】使用线程池
        executor.submit(() -> {
            try {
                String resp = get("/users/me");
                JsonNode node = mapper.readTree(resp);
                if (node.get("code").asInt() == 1) {
                    JsonNode data = node.get("data");
                    currentUserNick = data.has("nickname") && !data.get("nickname").isNull()
                            ? data.get("nickname").asText() : data.get("username").asText();
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void loadSessions() {
        // 【优化】使用线程池
        executor.submit(() -> {
            try {
                String resp = get("/sessions");
                JsonNode node = mapper.readTree(resp);
                if (node.get("code").asInt() == 1) {
                    SwingUtilities.invokeLater(() -> {
                        chatListPanel.removeAll();
                        JsonNode sessions = node.get("data");
                        if (sessions.isArray()) {
                            for (JsonNode session : sessions) {
                                addSessionItem(session.get("id").asText(), session.get("title").asText());
                            }
                        }
                        chatListPanel.revalidate();
                        chatListPanel.repaint();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void addSessionItem(String id, String title) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        item.setBackground(Color.WHITE);
        item.setBorder(new EmptyBorder(10, 15, 10, 15));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        item.add(titleLbl, BorderLayout.CENTER);

        // 鼠标悬停效果和点击事件
        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { item.setBackground(new Color(245, 245, 245)); }
            public void mouseExited(MouseEvent e) {
                if(!id.equals(currentSessionId)) item.setBackground(Color.WHITE);
            }
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showSessionMenu(e, id); // 右键删除
                } else {
                    switchSession(id, title, item);
                }
            }
        });

        chatListPanel.add(item);
    }

    private void startNewChat() {
        currentSessionId = null;
        titleLabel.setText("新会话");
        chatBoxPanel.removeAll();
        // 添加一个 AI 开场白
        addMessageBubble("你好！我是 Novi，很高兴见到你。", false);
        chatBoxPanel.revalidate();
        chatBoxPanel.repaint();
        // 重置选中状态
        for(Component c : chatListPanel.getComponents()) c.setBackground(Color.WHITE);
    }

    private void switchSession(String id, String title, JPanel itemPanel) {
        currentSessionId = id;
        titleLabel.setText(title);

        // 高亮当前项
        for(Component c : chatListPanel.getComponents()) c.setBackground(Color.WHITE);
        itemPanel.setBackground(new Color(230, 240, 255));

        chatBoxPanel.removeAll();
        chatBoxPanel.revalidate();
        chatBoxPanel.repaint();

        // 加载历史消息 【优化】使用线程池
        executor.submit(() -> {
            try {
                String resp = get("/sessions/" + id + "/messages");
                JsonNode node = mapper.readTree(resp);
                if (node.get("code").asInt() == 1) {
                    JsonNode msgs = node.get("data");
                    SwingUtilities.invokeLater(() -> {
                        for (JsonNode msg : msgs) {
                            boolean isUser = "USER".equalsIgnoreCase(msg.get("role").asText());
                            addMessageBubble(msg.get("content").asText(), isUser);
                        }
                        scrollToBottom();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void showSessionMenu(MouseEvent e, String sessionId) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem delItem = new JMenuItem("删除会话");
        delItem.addActionListener(ev -> {
            // 【优化】使用线程池
            executor.submit(() -> {
                try {
                    Request req = new Request.Builder()
                            .url(API_BASE + "/sessions/" + sessionId)
                            .delete().header("Authorization", "Bearer " + jwtToken).build();
                    client.newCall(req).execute();
                    SwingUtilities.invokeLater(() -> {
                        loadSessions(); // 刷新列表
                        if(sessionId.equals(currentSessionId)) startNewChat();
                    });
                } catch(Exception ex) { ex.printStackTrace(); }
            });
        });
        menu.add(delItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    // --- 核心：发送与流式接收 ---
    private void sendMessage() {
        String content = inputField.getText().trim();
        if (content.isEmpty()) return;

        // 1. 上屏用户消息
        addMessageBubble(content, true);
        inputField.setText("");
        inputField.setEnabled(false); // 发送中禁止输入

        // 2. 准备 AI 气泡 (初始为空)
        JTextArea aiBubble = addMessageBubble("", false);

        // 【优化】使用线程池
        executor.submit(() -> {
            try {
                String jsonBody = String.format("{\"message\":\"%s\", \"sessionId\": %s}",
                        content.replace("\n", "\\n").replace("\"", "\\\""), // 简单转义
                        currentSessionId == null ? "null" : "\"" + currentSessionId + "\"");

                Request request = new Request.Builder()
                        .url(API_BASE + "/chat/send/stream")
                        .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                        .header("Authorization", "Bearer " + jwtToken)
                        .build();

                // 使用 try-with-resources 自动关闭 Response
                try (Response response = client.newCall(request).execute()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                    String line;
                    StringBuilder fullContent = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        // SSE 格式解析
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if(data.isEmpty()) continue;
                            try {
                                JsonNode event = mapper.readTree(data);
                                String type = event.get("eventType").asText();

                                if ("CONTENT".equals(type)) {
                                    String chunk = event.get("content").asText();
                                    fullContent.append(chunk);
                                    // 实时更新 UI
                                    SwingUtilities.invokeLater(() -> {
                                        aiBubble.setText(fullContent.toString());
                                        // 气泡大小会随内容变化，重新布局
                                        aiBubble.revalidate();
                                        scrollToBottom();
                                    });
                                } else if ("METADATA".equals(type)) {
                                    // 自动保存新会话 ID
                                    if(event.has("sessionId")) currentSessionId = event.get("sessionId").asText();
                                    if(event.has("title")) {
                                        String newTitle = event.get("title").asText();
                                        SwingUtilities.invokeLater(() -> {
                                            titleLabel.setText(newTitle);
                                            loadSessions(); // 刷新列表以显示新标题
                                        });
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> aiBubble.setText("网络错误: " + e.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> {
                    inputField.setEnabled(true);
                    inputField.requestFocus();
                });
            }
        });
    }

    // ==================== 4. 辅助方法与渲染 ====================

    /**
     * 添加消息气泡
     * @return 如果是 AI 消息，返回用于流式更新的 JTextArea，否则返回 null
     */
    private JTextArea addMessageBubble(String text, boolean isUser) {
        JPanel rowPanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
        rowPanel.setBackground(COLOR_BG_MAIN);
        rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        // 头像
        JLabel avatar = new JLabel(isUser ? " U " : " AI ");
        avatar.setOpaque(true);
        avatar.setBackground(isUser ? COLOR_PRIMARY : Color.LIGHT_GRAY);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Consolas", Font.BOLD, 12));
        avatar.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 气泡内容：使用 JTextArea 实现自动换行
        JTextArea bubble = new JTextArea(text);
        bubble.setLineWrap(true);
        bubble.setWrapStyleWord(true);
        bubble.setEditable(false);
        bubble.setOpaque(true);
        bubble.setBackground(isUser ? COLOR_BG_USER : COLOR_BG_AI);
        bubble.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // 限制气泡最大宽度
        int maxWidth = 500;
        // 简单的动态大小计算 (Swing 的 BoxLayout 需要 PreferredSize)
        bubble.setSize(new Dimension(maxWidth, Short.MAX_VALUE));

        // 组装
        if (isUser) {
            rowPanel.add(bubble);
            rowPanel.add(avatar);
        } else {
            rowPanel.add(avatar);
            rowPanel.add(bubble);
        }

        chatBoxPanel.add(rowPanel);
        chatBoxPanel.add(Box.createVerticalStrut(10)); // 间距

        scrollToBottom();
        chatBoxPanel.revalidate();
        chatBoxPanel.repaint();

        return bubble;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    // HTTP POST 辅助方法
    private String post(String endpoint, String json) throws IOException {
        Request request = new Request.Builder()
                .url(API_BASE + endpoint)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }

    // HTTP GET 辅助方法
    private String get(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(API_BASE + endpoint)
                .header("Authorization", "Bearer " + jwtToken)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));

        // 【修复】macOS下默认LookAndFeel会忽略setBackground，导致白色文字显示在浅灰背景上看不清
        // 强制设置为不透明并取消默认边框绘制，以显示自定义背景色
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleField(JTextField field, String title) {
        field.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), title));
        field.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
    }

    private void showError(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "错误", JOptionPane.ERROR_MESSAGE));
    }
}