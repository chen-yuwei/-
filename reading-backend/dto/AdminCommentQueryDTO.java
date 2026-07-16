package com.example.reading.dto;

import lombok.Data;

@Data
public class AdminCommentQueryDTO {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long bookId;
    private Long userId;
    private Integer status;
}
