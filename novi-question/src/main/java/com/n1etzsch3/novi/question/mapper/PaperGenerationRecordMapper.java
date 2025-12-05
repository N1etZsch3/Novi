package com.n1etzsch3.novi.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.common.pojo.entity.PaperGenerationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 套卷生成记录 Mapper 接口
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
@Mapper
public interface PaperGenerationRecordMapper extends BaseMapper<PaperGenerationRecord> {
}
