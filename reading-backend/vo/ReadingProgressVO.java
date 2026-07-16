package com.example.reading.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReadingProgressVO {

    private Long id;
    private Long bookId;
    private Long chapterId;
    private Integer chapterNo;
    private String chapterTitle;
    private Integer chapterOffset;
    private BigDecimal progressPercent;
    private LocalDateTime lastReadAt;
}
