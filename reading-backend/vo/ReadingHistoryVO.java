package com.example.reading.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingHistoryVO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String coverUrl;
    private Long chapterId;
    private String chapterTitle;
    private Integer chapterNo;
    private Integer durationSeconds;
    private LocalDateTime readAt;
}
