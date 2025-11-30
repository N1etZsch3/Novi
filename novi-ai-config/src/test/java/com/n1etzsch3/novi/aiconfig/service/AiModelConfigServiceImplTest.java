package com.n1etzsch3.novi.aiconfig.service;

import com.n1etzsch3.novi.common.pojo.entity.AiModelConfig;
import com.n1etzsch3.novi.aiconfig.mapper.AiModelConfigMapper;
import com.n1etzsch3.novi.aiconfig.service.impl.AiModelConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiModelConfigServiceImplTest {

    @Mock
    private AiModelConfigMapper aiModelConfigMapper;

    @InjectMocks
    private AiModelConfigServiceImpl aiModelConfigService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Initialize TableInfo for MyBatis-Plus LambdaWrapper to work
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper
                .initTableInfo(new org.apache.ibatis.builder.MapperBuilderAssistant(
                        new com.baomidou.mybatisplus.core.MybatisConfiguration(), ""), AiModelConfig.class);
    }

    @Test
    void switchModelByName_Success() {
        // Mock active model
        AiModelConfig activeModel = new AiModelConfig();
        activeModel.setId(1L);
        activeModel.setModelName("OldModel");
        activeModel.setIsActive(true);

        // Mock target model
        AiModelConfig targetModel = new AiModelConfig();
        targetModel.setId(2L);
        targetModel.setModelName("NewModel");
        targetModel.setIsActive(false);

        // Mock sequential calls:
        // 1. getActiveModel -> selectOne (returns activeModel)
        // 2. switchModelByName -> selectOne (returns targetModel)
        when(aiModelConfigMapper.selectOne(any()))
                .thenReturn(activeModel)
                .thenReturn(targetModel);

        // Execute
        boolean result = aiModelConfigService.switchModelByName("NewModel");

        // Verify
        assertTrue(result);
        verify(aiModelConfigMapper, times(2)).update(any(), any());
    }
}
