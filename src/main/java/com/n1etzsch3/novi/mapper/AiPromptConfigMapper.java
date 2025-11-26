package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.AiPromptConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface AiPromptConfigMapper {

    /**
     * 根据 Key 查找配置。
     *
     * @param configKey 唯一的配置 Key。
     * @return 配置实体，如果未找到则返回 null。
     */
    AiPromptConfig findByKey(String configKey);

    /**
     * 查找所有配置。
     *
     * @return 所有配置的列表。
     */
    List<AiPromptConfig> findAll();

    /**
     * 更新配置值。
     *
     * @param configKey   要更新的配置 Key。
     * @param configValue 新的值。
     * @return 受影响的行数。
     */
    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 插入新配置。
     *
     * @param config 要插入的配置实体。
     * @return 受影响的行数。
     */
    int insert(AiPromptConfig config);

    /**
     * 根据 Key 删除配置。
     *
     * @param configKey 要删除的配置 Key。
     * @return 受影响的行数。
     */
    int deleteByKey(String configKey);

    /**
     * 根据类型查找配置。
     *
     * @param configType 配置类型 (例如: 0:系统, 1:性格, 2:语气风格)。
     * @return 匹配的配置列表。
     */
    List<AiPromptConfig> findByType(Integer configType);
}
