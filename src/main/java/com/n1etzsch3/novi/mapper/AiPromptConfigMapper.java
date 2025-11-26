package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.AiPromptConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 提示词配置 Mapper
 * <p>
 * AI 提示词配置的数据访问对象。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Mapper
public interface AiPromptConfigMapper extends BaseMapper<AiPromptConfig> {

    /**
     * 根据类型查找配置。
     *
     * @param configType 配置类型 (例如: 0:系统, 1:性格, 2:语气风格)。
     * @return 匹配的配置列表。
     */
    @Select("SELECT * FROM ai_prompt_config WHERE config_type = #{configType}")
    List<AiPromptConfig> findByType(Integer configType);
}
