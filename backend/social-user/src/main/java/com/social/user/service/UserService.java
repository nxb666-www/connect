package com.social.user.service;

import com.social.user.dto.UserLoginDTO;
import com.social.user.dto.UserRegisterDTO;
import com.social.user.dto.UserUpdateDTO;
import com.social.user.vo.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    Long register(UserRegisterDTO dto);

    UserVO login(UserLoginDTO dto);

    UserVO getUserInfo(Long userId);

    void updateUser(Long userId, UserUpdateDTO dto);

    List<UserVO> searchUser(String keyword);

    Map<String, Object> getUserStats(String date);

    void updateAvatar(Long userId, String avatarUrl);

    List<UserVO> getAllUsers();

    void updateUserStatus(Long userId, Integer status);
}
