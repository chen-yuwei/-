package com.example.reading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChapterSaveDTO {

    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @NotNull(message = "章节序号不能为空")
    private Integer chapterNo;

    @NotBlank(message = "章节标题不能为空")
    @Size(max = 200, message = "章节标题不能超过200个字符")
    private String chapterTitle;

    @NotBlank(message = "章节内容不能为空")
    private String content;

    @NotNull(message = "是否免费不能为空")
    private Integer isFree;

    @NotNull(message = "发布状态不能为空")
    private Integer publishStatus;
}
