package com.example.reading.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {

    private Long id;
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private List<CategoryVO> children;
}
