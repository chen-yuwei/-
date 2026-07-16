package com.example.reading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReadingHistoryCreateDTO {

    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @Min(value = 0, message = "阅读时长不能为负数")
    private Integer durationSeconds;
}
