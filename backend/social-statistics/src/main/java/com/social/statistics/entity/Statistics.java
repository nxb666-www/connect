package com.social.statistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("statistics")
public class Statistics implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate date;

    private Integer newUsers;

    private Integer activeUsers;

    private Integer newPosts;

    private Integer totalLikes;

    private Integer totalComments;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
