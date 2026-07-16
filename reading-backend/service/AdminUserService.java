package com.example.reading.service;

import com.example.reading.common.PageResult;
import com.example.reading.dto.AdminUserQueryDTO;
import com.example.reading.vo.UserVO;

public interface AdminUserService {

    PageResult<UserVO> listUsers(AdminUserQueryDTO query);

    UserVO getUserDetail(Long id);

    void updateUserStatus(Long operatorId, Long userId, Integer status);
}
