package com.example.reading.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChapterVO {

    private Long id;
    private Long bookId;
    private Integer chapterNo;
    private String chapterTitle;
    private String content;
    private Integer wordCount;
    private Integer isFree;
    private Integer publishStatus;
    private LocalDateTime publishedAt;
    private String bookTitle;
    private Boolean isCurrent;
}
