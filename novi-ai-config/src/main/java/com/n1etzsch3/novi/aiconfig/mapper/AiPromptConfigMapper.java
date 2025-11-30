package com.n1etzsch3.novi.aiconfig.mapper;

import com.n1etzsch3.novi.common.pojo.entity.AiPromptConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

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
}
