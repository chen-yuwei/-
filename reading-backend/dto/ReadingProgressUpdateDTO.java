package com.example.reading.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReadingProgressUpdateDTO {

    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    private Integer chapterOffset;

    @DecimalMin(value = "0.00", message = "进度不能小于0")
    @DecimalMax(value = "100.00", message = "进度不能大于100")
    private BigDecimal progressPercent;
}
