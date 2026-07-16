package com.example.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reading.common.ResultCode;
import com.example.reading.dto.ReadingProgressUpdateDTO;
import com.example.reading.entity.Bookshelf;
import com.example.reading.entity.Chapter;
import com.example.reading.entity.ReadingProgress;
import com.example.reading.exception.BusinessException;
import com.example.reading.mapper.BookshelfMapper;
import com.example.reading.mapper.ChapterMapper;
import com.example.reading.mapper.ReadingProgressMapper;
import com.example.reading.service.ReadingProgressService;
import com.example.reading.vo.ReadingProgressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingProgressServiceImpl implements ReadingProgressService {

    private final ReadingProgressMapper readingProgressMapper;
    private final ChapterMapper chapterMapper;
    private final BookshelfMapper bookshelfMapper;

    @Override
    public ReadingProgressVO getProgress(Long userId, Long bookId) {
        ReadingProgress progress = readingProgressMapper.selectOne(new LambdaQueryWrapper<ReadingProgress>()
                .eq(ReadingProgress::getUserId, userId)
                .eq(ReadingProgress::getBookId, bookId));
        if (progress == null) {
            return null;
        }
        return toVO(progress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateProgress(Long userId, ReadingProgressUpdateDTO dto) {
        Chapter chapter = chapterMapper.selectById(dto.getChapterId());
        if (chapter == null || chapter.getPublishStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在或未发布");
        }
        if (!chapter.getBookId().equals(dto.getBookId())) {
            throw new BusinessException("章节与图书不匹配");
        }

        ReadingProgress progress = readingProgressMapper.selectOne(new LambdaQueryWrapper<ReadingProgress>()
                .eq(ReadingProgress::getUserId, userId)
                .eq(ReadingProgress::getBookId, dto.getBookId()));

        if (progress == null) {
            progress = new ReadingProgress();
            progress.setUserId(userId);
            progress.setBookId(dto.getBookId());
        }

        progress.setChapterId(dto.getChapterId());
        progress.setChapterOffset(dto.getChapterOffset() != null ? dto.getChapterOffset() : 0);
        progress.setProgressPercent(dto.getProgressPercent() != null ? dto.getProgressPercent() : BigDecimal.ZERO);
        progress.setLastReadAt(LocalDateTime.now());

        if (progress.getId() == null) {
            readingProgressMapper.insert(progress);
        } else {
            readingProgressMapper.updateById(progress);
        }

        // 同步更新书架阅读状态和时间
        Bookshelf bookshelf = bookshelfMapper.selectOne(new LambdaQueryWrapper<Bookshelf>()
                .eq(Bookshelf::getUserId, userId)
                .eq(Bookshelf::getBookId, dto.getBookId()));
        if (bookshelf != null) {
            bookshelf.setLastReadAt(LocalDateTime.now());
            if (bookshelf.getReadingStatus() == null || bookshelf.getReadingStatus() == 0) {
                bookshelf.setReadingStatus(1);
            }
            if (dto.getProgressPercent() != null && dto.getProgressPercent().compareTo(new BigDecimal("100")) >= 0) {
                bookshelf.setReadingStatus(2);
            }
            bookshelfMapper.updateById(bookshelf);
        }
    }

    private ReadingProgressVO toVO(ReadingProgress progress) {
        ReadingProgressVO vo = new ReadingProgressVO();
        BeanUtils.copyProperties(progress, vo);
        Chapter chapter = chapterMapper.selectById(progress.getChapterId());
        if (chapter != null) {
            vo.setChapterNo(chapter.getChapterNo());
            vo.setChapterTitle(chapter.getChapterTitle());
        }
        return vo;
    }
}
