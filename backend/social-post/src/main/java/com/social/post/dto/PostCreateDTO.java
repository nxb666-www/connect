package com.social.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PostCreateDTO implements Serializable {

    @NotBlank(message = "动态内容不能为空")
    @Size(max = 2000, message = "动态内容不能超过2000字")
    private String content;

    private List<String> images;

    private String topic;

    private Integer visibility = 1;
}
