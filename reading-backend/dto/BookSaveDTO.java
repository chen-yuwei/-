package com.example.reading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BookSaveDTO {

    @NotBlank(message = "图书名称不能为空")
    @Size(max = 200, message = "图书名称不能超过200个字符")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 100, message = "作者不能超过100个字符")
    private String author;

    private String coverUrl;

    private String summary;

    private String isbn;

    private String publisher;

    @NotNull(message = "连载状态不能为空")
    private Integer serializeStatus;

    @NotNull(message = "发布状态不能为空")
    private Integer publishStatus;

    private Integer isRecommended;

    @NotNull(message = "分类不能为空")
    private List<Long> categoryIds;
}
