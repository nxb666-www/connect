package com.social.social.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("follow")
public class Follow implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long followId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
