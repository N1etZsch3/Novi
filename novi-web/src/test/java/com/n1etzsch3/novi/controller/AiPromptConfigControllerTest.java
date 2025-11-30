package com.n1etzsch3.novi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.common.pojo.entity.AiPromptConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("AI 提示词配置控制器 (AiPromptConfigController) 测试")
public class AiPromptConfigControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("成功添加配置")
        public void testAddConfigSuccess() throws Exception {
                String key = "test_key_" + java.util.UUID.randomUUID().toString();
                AiPromptConfig config = new AiPromptConfig();
                config.setConfigKey(key);
                config.setConfigValue("test_value");
                config.setDescription("Test Description");
                config.setConfigType(1); // 1: Personality

                mockMvc.perform(post("/api/prompt/config")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(config)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1));
        }

        @Test
        @DisplayName("成功删除配置")
        public void testRemoveConfigSuccess() throws Exception {
                String key = "delete_key_" + java.util.UUID.randomUUID().toString();
                // 1. Add a config first
                AiPromptConfig config = new AiPromptConfig();
                config.setConfigKey(key);
                config.setConfigValue("delete_value");
                config.setDescription("To be deleted");
                config.setConfigType(1);

                mockMvc.perform(post("/api/prompt/config")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(config)))
                                .andExpect(status().isOk());

                // 2. Delete it
                mockMvc.perform(delete("/api/prompt/config/" + key))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1));
        }

        @Test
        @DisplayName("成功根据类型列出配置")
        public void testListConfigsByTypeSuccess() throws Exception {
                // 1. Add a config of type 2
                AiPromptConfig config = new AiPromptConfig();
                config.setConfigKey("type_test_key");
                config.setConfigValue("type_test_value");
                config.setDescription("Type Test");
                config.setConfigType(2); // 2: Tone Style

                mockMvc.perform(post("/api/prompt/config")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(config)))
                                .andExpect(status().isOk());

                // 2. List configs of type 2
                mockMvc.perform(get("/api/prompt/config/type/2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[?(@.configKey == 'type_test_key')]").exists());
        }
}
