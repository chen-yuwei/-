package com.example.reading.service;

import com.example.reading.dto.PasswordUpdateDTO;
import com.example.reading.dto.UserProfileUpdateDTO;
import com.example.reading.vo.UserVO;

public interface UserService {

    UserVO getProfile(Long userId);

    UserVO updateProfile(Long userId, UserProfileUpdateDTO dto);

    void updatePassword(Long userId, PasswordUpdateDTO dto);
}
