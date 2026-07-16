package com.example.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reading.entity.Book;
import com.example.reading.entity.BookCategory;
import com.example.reading.entity.BookComment;
import com.example.reading.entity.Category;
import com.example.reading.entity.Chapter;
import com.example.reading.entity.SysUser;
import com.example.reading.mapper.BookCategoryMapper;
import com.example.reading.mapper.BookCommentMapper;
import com.example.reading.mapper.BookMapper;
import com.example.reading.mapper.CategoryMapper;
import com.example.reading.mapper.ChapterMapper;
import com.example.reading.mapper.SysUserMapper;
import com.example.reading.service.AdminStatisticsService;
import com.example.reading.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final SysUserMapper sysUserMapper;
    private final BookMapper bookMapper;
    private final ChapterMapper chapterMapper;
    private final BookCommentMapper bookCommentMapper;
    private final CategoryMapper categoryMapper;
    private final BookCategoryMapper bookCategoryMapper;

    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO vo = new StatisticsVO();
        vo.setUserCount(sysUserMapper.selectCount(null));
        vo.setBookCount(bookMapper.selectCount(null));
        vo.setChapterCount(chapterMapper.selectCount(null));
        vo.setCommentCount(bookCommentMapper.selectCount(new LambdaQueryWrapper<BookComment>()
                .isNull(BookComment::getParentId)));

        List<Book> books = bookMapper.selectList(null);
        vo.setTotalViewCount(books.stream().mapToLong(b -> b.getViewCount() != null ? b.getViewCount() : 0).sum());
        vo.setTotalFavoriteCount(books.stream().mapToLong(b -> b.getFavoriteCount() != null ? b.getFavoriteCount() : 0).sum());

        vo.setRecentUserStats(fillRecentDays(bookMapper.selectRecentUserStats(), "count"));
        vo.setRecentViewStats(fillRecentDays(bookMapper.selectRecentViewStats(), "count"));
        vo.setCategoryStats(buildCategoryStats());
        return vo;
    }

    private List<Map<String, Object>> fillRecentDays(List<Map<String, Object>> stats, String valueKey) {
        Map<String, Long> statMap = stats.stream().collect(Collectors.toMap(
                m -> String.valueOf(m.get("statDate")),
                m -> ((Number) m.get(valueKey)).longValue(),
                (a, b) -> b
        ));

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", statMap.getOrDefault(date.toString(), 0L));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildCategoryStats() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Category category : categories) {
            Long bookCount = bookCategoryMapper.selectCount(new LambdaQueryWrapper<BookCategory>()
                    .eq(BookCategory::getCategoryId, category.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("categoryName", category.getCategoryName());
            item.put("count", bookCount);
            result.add(item);
        }
        return result;
    }
}
