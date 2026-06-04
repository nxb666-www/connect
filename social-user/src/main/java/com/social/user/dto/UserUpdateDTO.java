package com.social.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {

    @Size(min = 1, max = 50, message = "昵称长度为1-50个字符")
    private String nickname;

    @Size(max = 255, message = "签名长度不能超过255个字符")
    private String signature;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
}
