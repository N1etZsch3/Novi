package com.n1etzsch3.novi.question.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.common.pojo.entity.QuestionGenerationRecord;
import com.n1etzsch3.novi.question.mapper.QuestionGenerationRecordMapper;
import com.n1etzsch3.novi.question.service.impl.QuestionGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionGenerationServiceImplBatchDeleteTest {

    @Mock
    private QuestionGenerationRecordMapper questionGenerationRecordMapper;

    @InjectMocks
    private QuestionGenerationServiceImpl questionGenerationService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Initialize TableInfo for MyBatis-Plus LambdaWrapper to work
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper
                .initTableInfo(
                        new org.apache.ibatis.builder.MapperBuilderAssistant(
                                new com.baomidou.mybatisplus.core.MybatisConfiguration(), ""),
                        QuestionGenerationRecord.class);
    }

    @Test
    void deleteGenerationRecords_Success() {
        Long userId = 1L;
        List<Long> recordIds = Arrays.asList(1L, 2L, 3L);

        // Mock delete
        when(questionGenerationRecordMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);

        // Execute
        questionGenerationService.deleteGenerationRecords(recordIds, userId);

        // Verify
        verify(questionGenerationRecordMapper).delete(any(LambdaQueryWrapper.class));
    }
}
