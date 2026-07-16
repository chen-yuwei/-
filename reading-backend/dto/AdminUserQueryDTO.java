package com.example.reading.dto;

import lombok.Data;

@Data
public class AdminUserQueryDTO {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username;
    private String nickname;
    private String role;
    private Integer status;
}
