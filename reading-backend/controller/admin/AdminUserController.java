package com.example.reading.controller.admin;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.dto.AdminUserQueryDTO;
import com.example.reading.security.SecurityUtils;
import com.example.reading.service.AdminUserService;
import com.example.reading.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端-用户管理")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "用户分页列表")
    @GetMapping
    public Result<PageResult<UserVO>> listUsers(AdminUserQueryDTO query) {
        return Result.success(adminUserService.listUsers(query));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUserDetail(@PathVariable Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminUserService.updateUserStatus(SecurityUtils.getCurrentUserId(), id, status);
        return Result.success();
    }
}
