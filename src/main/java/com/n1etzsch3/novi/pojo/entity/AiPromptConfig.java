package com.n1etzsch3.novi.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiPromptConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
