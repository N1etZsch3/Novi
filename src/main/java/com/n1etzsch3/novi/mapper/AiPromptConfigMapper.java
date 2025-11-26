package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.AiPromptConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiPromptConfigMapper {

    /**
     * 根据Key查询配置
     */
    AiPromptConfig findByKey(String configKey);

    /**
     * 查询所有配置
     */
    List<AiPromptConfig> findAll();

    /**
     * 更新配置值
     */
    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
