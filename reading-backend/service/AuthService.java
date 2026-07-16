package com.example.reading.service;

import com.example.reading.dto.LoginDTO;
import com.example.reading.dto.RegisterDTO;
import com.example.reading.vo.LoginVO;
import com.example.reading.vo.UserVO;

public interface AuthService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO getCurrentUser();
}
