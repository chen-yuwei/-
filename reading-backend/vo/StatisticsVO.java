package com.example.reading.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StatisticsVO {

    private Long userCount;
    private Long bookCount;
    private Long chapterCount;
    private Long commentCount;
    private Long totalViewCount;
    private Long totalFavoriteCount;
    private List<Map<String, Object>> recentUserStats;
    private List<Map<String, Object>> recentViewStats;
    private List<Map<String, Object>> categoryStats;
}
