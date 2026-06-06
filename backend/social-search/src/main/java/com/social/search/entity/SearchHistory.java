package com.social.search.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("search_history")
public class SearchHistory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String keyword;

    private String type;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
