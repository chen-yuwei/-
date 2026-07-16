package com.example.reading.dto;

import lombok.Data;

@Data
public class BookQueryDTO {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long categoryId;
    private String title;
    private String author;
    private String keyword;
    private String sortField = "updatedAt";
    private String sortOrder = "desc";
}
