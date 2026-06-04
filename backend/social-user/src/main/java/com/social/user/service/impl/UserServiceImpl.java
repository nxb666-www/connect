package com.social.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.social.common.core.exception.BusinessException;
import com.social.common.redis.service.RedisService;
import com.social.common.security.utils.JwtUtils;
import com.social.user.dto.UserLoginDTO;
import com.social.user.dto.UserRegisterDTO;
import com.social.user.dto.UserUpdateDTO;
import com.social.user.entity.User;
import com.social.user.mapper.UserMapper;
import com.social.user.service.UserService;
import com.social.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RedisService redisService;
    private final JwtUtils jwtUtils;

    @Override
    public Long register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setLevel(1);
        user.setPoints(0);
        user.setStatus(1);
        user.setDeleted(0);

        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public UserVO login(UserLoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成JWT token
        String token = jwtUtils.generateToken(user.getId());

        // 存储到Redis
        redisService.set("token:" + user.getId(), token, 24, TimeUnit.HOURS);

        // 返回用户信息
        return convertToVO(user, token);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user, null);
    }

    @Override
    public Map<String, Object> getUserStats(String date) {
        LambdaQueryWrapper<User> newUsersWrapper = new LambdaQueryWrapper<>();
        newUsersWrapper.apply("DATE(create_time) = {0}", date);
        long newUsers = userMapper.selectCount(newUsersWrapper);

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("newUsers", newUsers);
        return stats;
    }

    @Override
    public List<UserVO> searchUser(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
                .last("LIMIT 20");
        return userMapper.selectList(wrapper).stream()
                .map(user -> convertToVO(user, null))
                .toList();
    }

    @Override
    public void updateUser(Long userId, UserUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getSignature() != null) {
            user.setSignature(dto.getSignature());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    @Override
    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(user -> convertToVO(user, null))
                .toList();
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private UserVO convertToVO(User user, String token) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setUserId(user.getId());
        vo.setToken(token);
        return vo;
    }
}
