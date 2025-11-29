package com.n1etzsch3.novi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.domain.po.AiModelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI模型配置 Mapper 接口
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {
}
