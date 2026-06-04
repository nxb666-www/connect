package com.social.user.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户VO
 */
@Data
public class UserVO implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String signature;
    private Integer level;
    private Integer points;
    private Integer status;
    private String role;
    private LocalDateTime createTime;
    private String token;
}
