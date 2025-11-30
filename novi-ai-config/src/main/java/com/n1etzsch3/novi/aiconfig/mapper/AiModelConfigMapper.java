package com.n1etzsch3.novi.aiconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.common.pojo.entity.AiModelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI模型配置 Mapper 接口
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {
    /**
     * 原子性切换模型：将指定名称的模型设为激活，其他设为非激活
     *
     * @param modelName 要激活的模型名称
     * @return 更新的行数
     */
    @org.apache.ibatis.annotations.Update("UPDATE ai_model_config SET is_active = CASE WHEN model_name = #{modelName} THEN 1 ELSE 0 END")
    int activateModelByName(@org.apache.ibatis.annotations.Param("modelName") String modelName);
}
