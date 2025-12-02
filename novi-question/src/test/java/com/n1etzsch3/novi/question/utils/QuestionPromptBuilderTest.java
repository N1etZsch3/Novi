package com.n1etzsch3.novi.question.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.aiconfig.mapper.AiPromptConfigMapper;
import com.n1etzsch3.novi.common.pojo.entity.AiPromptConfig;
import com.n1etzsch3.novi.common.pojo.entity.QuestionExample;
import com.n1etzsch3.novi.question.mapper.QuestionCategoryMapper;
import com.n1etzsch3.novi.question.pojo.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.question.pojo.entity.QuestionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionPromptBuilderTest {

    @Mock
    private AiPromptConfigMapper aiPromptConfigMapper;

    @Mock
    private QuestionCategoryMapper questionCategoryMapper;

    @InjectMocks
    private QuestionPromptBuilder questionPromptBuilder;

    private QuestionGenerationRequest request;
    private List<QuestionExample> examples;

    @BeforeEach
    void setUp() {
        request = new QuestionGenerationRequest();
        request.setSubject("湖北专升本英语");
        request.setQuestionType("连词成句");
        request.setDifficulty("medium");
        request.setQuantity(5);
        request.setTheme("测试主题");

        QuestionExample example = new QuestionExample();
        example.setContent("Example Content");
        examples = Collections.singletonList(example);
    }

    @Test
    void buildPrompt_Success() {
        // Mock Category lookup
        QuestionCategory subjectCat = new QuestionCategory();
        subjectCat.setCode("english_hubei");
        when(questionCategoryMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(subjectCat) // First call for subject
                .thenReturn(new QuestionCategory() {
                    {
                        setCode("sentence_ordering");
                    }
                }); // Second call for type

        // Mock Template lookup
        AiPromptConfig templateConfig = new AiPromptConfig();
        templateConfig.setConfigValue(
                "Role: Expert. Task: Generate {quantity} {difficulty} questions about {theme}. Subject: {subject}. Examples: {examples}");
        when(aiPromptConfigMapper.selectById("prompt:english_hubei:sentence_ordering")).thenReturn(templateConfig);

        // Mock Difficulty lookup
        AiPromptConfig difficultyConfig = new AiPromptConfig();
        difficultyConfig.setConfigValue("Medium Difficulty Description");
        when(aiPromptConfigMapper.selectById("desc:difficulty:english_hubei:sentence_ordering:medium"))
                .thenReturn(difficultyConfig);

        // Execute
        String prompt = questionPromptBuilder.buildPrompt(request, examples);

        // Verify
        assertNotNull(prompt);
        assertTrue(prompt.contains("Generate 5 Medium Difficulty Description questions"));
        assertTrue(prompt.contains("Subject: 湖北专升本英语"));
        assertTrue(prompt.contains("Example Content"));
    }

    @Test
    void buildPrompt_MissingCategory_ThrowsException() {
        when(questionCategoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            questionPromptBuilder.buildPrompt(request, examples);
        });
    }

    @Test
    void buildPrompt_MissingTemplate_ThrowsException() {
        // Mock Category lookup
        QuestionCategory subjectCat = new QuestionCategory();
        subjectCat.setCode("english_hubei");
        when(questionCategoryMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(subjectCat)
                .thenReturn(new QuestionCategory() {
                    {
                        setCode("sentence_ordering");
                    }
                });

        // Mock Template lookup (return null)
        when(aiPromptConfigMapper.selectById("prompt:english_hubei:sentence_ordering")).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> {
            questionPromptBuilder.buildPrompt(request, examples);
        });
    }

    @Test
    void buildPrompt_MissingDifficulty_UsesDefault() {
        // Mock Category lookup
        QuestionCategory subjectCat = new QuestionCategory();
        subjectCat.setCode("english_hubei");
        when(questionCategoryMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(subjectCat)
                .thenReturn(new QuestionCategory() {
                    {
                        setCode("sentence_ordering");
                    }
                });

        // Mock Template lookup
        AiPromptConfig templateConfig = new AiPromptConfig();
        templateConfig.setConfigValue("Difficulty: {difficulty}");
        when(aiPromptConfigMapper.selectById("prompt:english_hubei:sentence_ordering")).thenReturn(templateConfig);

        // Mock Difficulty lookup (return null)
        when(aiPromptConfigMapper.selectById("desc:difficulty:english_hubei:sentence_ordering:medium"))
                .thenReturn(null);

        // Execute
        String prompt = questionPromptBuilder.buildPrompt(request, examples);

        // Verify
        assertTrue(prompt.contains("Difficulty: 难度适中，符合考试大纲要求。"));
    }
}
