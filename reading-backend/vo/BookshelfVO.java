package com.example.reading.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookshelfVO {

    private Long id;
    private Long bookId;
    private String title;
    private String author;
    private String coverUrl;
    private Integer readingStatus;
    private LocalDateTime lastReadAt;
    private BigDecimal progressPercent;
    private Long currentChapterId;
    private String currentChapterTitle;
}
