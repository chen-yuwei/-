package com.example.reading.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    private String avatarUrl;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;
}
