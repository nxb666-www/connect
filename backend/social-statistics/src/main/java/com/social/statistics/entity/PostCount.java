package com.social.statistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("post")
public class PostCount {
    private Long id;
}
