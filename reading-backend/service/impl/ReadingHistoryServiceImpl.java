package com.example.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reading.common.PageResult;
import com.example.reading.common.ResultCode;
import com.example.reading.dto.ReadingHistoryCreateDTO;
import com.example.reading.entity.Book;
import com.example.reading.entity.Chapter;
import com.example.reading.entity.ReadingHistory;
import com.example.reading.exception.BusinessException;
import com.example.reading.mapper.BookMapper;
import com.example.reading.mapper.ChapterMapper;
import com.example.reading.mapper.ReadingHistoryMapper;
import com.example.reading.service.ReadingHistoryService;
import com.example.reading.vo.ReadingHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private final ReadingHistoryMapper readingHistoryMapper;
    private final BookMapper bookMapper;
    private final ChapterMapper chapterMapper;

    @Override
    public PageResult<ReadingHistoryVO> listHistory(Long userId, Integer pageNum, Integer pageSize) {
        Page<ReadingHistory> page = new Page<>(pageNum, pageSize);
        Page<ReadingHistory> result = readingHistoryMapper.selectPage(page, new LambdaQueryWrapper<ReadingHistory>()
                .eq(ReadingHistory::getUserId, userId)
                .orderByDesc(ReadingHistory::getReadAt));

        List<ReadingHistoryVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public void addHistory(Long userId, ReadingHistoryCreateDTO dto) {
        Chapter chapter = chapterMapper.selectById(dto.getChapterId());
        if (chapter == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在");
        }

        ReadingHistory history = new ReadingHistory();
        history.setUserId(userId);
        history.setBookId(dto.getBookId());
        history.setChapterId(dto.getChapterId());
        history.setDurationSeconds(dto.getDurationSeconds() != null ? dto.getDurationSeconds() : 0);
        readingHistoryMapper.insert(history);
    }

    @Override
    public void deleteHistory(Long userId, Long id) {
        ReadingHistory history = readingHistoryMapper.selectById(id);
        if (history == null || !history.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "阅读历史不存在");
        }
        readingHistoryMapper.deleteById(id);
    }

    @Override
    public void clearHistory(Long userId) {
        readingHistoryMapper.delete(new LambdaQueryWrapper<ReadingHistory>()
                .eq(ReadingHistory::getUserId, userId));
    }

    private ReadingHistoryVO toVO(ReadingHistory history) {
        ReadingHistoryVO vo = new ReadingHistoryVO();
        vo.setId(history.getId());
        vo.setBookId(history.getBookId());
        vo.setChapterId(history.getChapterId());
        vo.setDurationSeconds(history.getDurationSeconds());
        vo.setReadAt(history.getReadAt());

        Book book = bookMapper.selectById(history.getBookId());
        if (book != null) {
            vo.setBookTitle(book.getTitle());
            vo.setCoverUrl(book.getCoverUrl());
        }
        Chapter chapter = chapterMapper.selectById(history.getChapterId());
        if (chapter != null) {
            vo.setChapterTitle(chapter.getChapterTitle());
            vo.setChapterNo(chapter.getChapterNo());
        }
        return vo;
    }
}
