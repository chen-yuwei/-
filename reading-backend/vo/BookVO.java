package com.example.reading.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookVO {

    private Long id;
    private String title;
    private String author;
    private String coverUrl;
    private String summary;
    private String isbn;
    private String publisher;
    private Integer totalChapters;
    private Long totalWords;
    private Integer serializeStatus;
    private Integer publishStatus;
    private Integer isRecommended;
    private Long viewCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private BigDecimal averageScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryVO> categories;
    private ChapterVO latestChapter;
    private Boolean inBookshelf;
    private ReadingProgressVO readingProgress;
}
