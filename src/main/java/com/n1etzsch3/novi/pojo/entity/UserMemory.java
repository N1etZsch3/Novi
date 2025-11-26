package com.n1etzsch3.novi.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("user_memory")
public class UserMemory {

        @TableId(type = IdType.AUTO)
        private Long id;

        private String factKey;

        private String factValue;

        private String factType;

        private LocalDateTime createdAt;

        private LocalDateTime lastAccessedAt;

        // --- 关系 ---

        private Long userId;

        @TableField(exist = false)
        @JsonIgnore
        private UserAccount user;

        private Long sourceMessageId;

        @TableField(exist = false)
        private ChatMessage sourceMessage;
}
