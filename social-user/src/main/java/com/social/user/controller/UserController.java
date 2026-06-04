package com.social.user.controller;

import com.social.common.core.result.Result;
import com.social.common.security.context.LoginUserContext;
import com.social.user.dto.UserLoginDTO;
import com.social.user.dto.UserRegisterDTO;
import com.social.user.dto.UserUpdateDTO;
import com.social.user.service.UserService;
import com.social.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${file.upload-dir:C:/uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8084}")
    private String baseUrl;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody UserRegisterDTO dto) {
        Long userId = userService.register(dto);
        return Result.success(userId);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody UserLoginDTO dto) {
        UserVO userVO = userService.login(dto);
        return Result.success(userVO);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUser() {
        Long userId = LoginUserContext.getUserId();
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserInfo(@PathVariable Long userId) {
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<List<UserVO>> searchUser(@RequestParam String keyword) {
        return Result.success(userService.searchUser(keyword));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public Result<Void> updateUser(@Valid @RequestBody UserUpdateDTO dto) {
        Long userId = LoginUserContext.getUserId();
        userService.updateUser(userId, dto);
        return Result.success();
    }

    @Operation(summary = "内部服务调用-获取用户信息")
    @GetMapping("/internal/{userId}")
    public Result<UserVO> getInternalUserInfo(@PathVariable Long userId) {
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "内部服务调用-搜索用户")
    @GetMapping("/internal/search")
    public Result<List<UserVO>> internalSearchUser(@RequestParam String keyword) {
        return Result.success(userService.searchUser(keyword));
    }

    @Operation(summary = "内部服务调用-获取统计数据")
    @GetMapping("/internal/stats")
    public Result<Map<String, Object>> getUserStats(@RequestParam String date) {
        return Result.success(userService.getUserStats(date));
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dirPath = uploadDir + "/" + datePath;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            file.transferTo(new File(dirPath + "/" + fileName));
        } catch (IOException e) {
            return Result.error("文件上传失败");
        }
        String url = baseUrl + "/uploads/" + datePath + "/" + fileName;
        Long userId = LoginUserContext.getUserId();
        userService.updateAvatar(userId, url);
        return Result.success(Map.of("url", url));
    }

    @Operation(summary = "管理员-获取所有用户")
    @GetMapping("/admin/list")
    public Result<List<UserVO>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @Operation(summary = "管理员-封禁/解封用户")
    @PutMapping("/admin/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success();
    }
}
