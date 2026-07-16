package com.example.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reading.common.PageResult;
import com.example.reading.common.ResultCode;
import com.example.reading.dto.ChapterSaveDTO;
import com.example.reading.entity.Book;
import com.example.reading.entity.Chapter;
import com.example.reading.exception.BusinessException;
import com.example.reading.entity.ReadingProgress;
import com.example.reading.mapper.BookMapper;
import com.example.reading.mapper.ChapterMapper;
import com.example.reading.mapper.ReadingProgressMapper;
import com.example.reading.service.BookService;
import com.example.reading.service.ChapterService;
import com.example.reading.vo.ChapterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterMapper chapterMapper;
    private final BookMapper bookMapper;
    private final ReadingProgressMapper readingProgressMapper;
    @Lazy
    private final BookService bookService;

    @Override
    public PageResult<ChapterVO> listChapters(Long bookId, Integer pageNum, Integer pageSize, Long userId, boolean onlyPublished) {
        validateBookExists(bookId);
        Page<Chapter> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Chapter> wrapper = new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, bookId)
                .orderByAsc(Chapter::getChapterNo);
        if (onlyPublished) {
            wrapper.eq(Chapter::getPublishStatus, 1);
        }
        Page<Chapter> result = chapterMapper.selectPage(page, wrapper);

        final Long progressChapterId = userId != null ? getProgressChapterId(userId, bookId) : null;
        List<ChapterVO> records = result.getRecords().stream().map(chapter -> {
            ChapterVO vo = toChapterVO(chapter, false);
            vo.setIsCurrent(progressChapterId != null && progressChapterId.equals(chapter.getId()));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ChapterVO getChapterDetail(Long chapterId, Long userId, String clientKey, boolean onlyPublished) {
        Chapter chapter = getChapterOrThrow(chapterId);
        if (onlyPublished && chapter.getPublishStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在或未发布");
        }
        Book book = bookMapper.selectById(chapter.getBookId());
        if (onlyPublished && (book == null || book.getPublishStatus() != 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图书不存在或已下架");
        }

        // 访问章节正文时更新阅读量（带去重）
        if (onlyPublished) {
            bookService.incrementViewCount(chapter.getBookId(), userId, clientKey);
        }

        ChapterVO vo = toChapterVO(chapter, true);
        if (book != null) {
            vo.setBookTitle(book.getTitle());
        }
        return vo;
    }

    @Override
    public ChapterVO getPreviousChapter(Long chapterId, boolean onlyPublished) {
        Chapter current = getChapterOrThrow(chapterId);
        LambdaQueryWrapper<Chapter> wrapper = new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, current.getBookId())
                .lt(Chapter::getChapterNo, current.getChapterNo())
                .orderByDesc(Chapter::getChapterNo)
                .last("LIMIT 1");
        if (onlyPublished) {
            wrapper.eq(Chapter::getPublishStatus, 1);
        }
        Chapter prev = chapterMapper.selectOne(wrapper);
        return prev != null ? toChapterVO(prev, onlyPublished) : null;
    }

    @Override
    public ChapterVO getNextChapter(Long chapterId, boolean onlyPublished) {
        Chapter current = getChapterOrThrow(chapterId);
        LambdaQueryWrapper<Chapter> wrapper = new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, current.getBookId())
                .gt(Chapter::getChapterNo, current.getChapterNo())
                .orderByAsc(Chapter::getChapterNo)
                .last("LIMIT 1");
        if (onlyPublished) {
            wrapper.eq(Chapter::getPublishStatus, 1);
        }
        Chapter next = chapterMapper.selectOne(wrapper);
        return next != null ? toChapterVO(next, onlyPublished) : null;
    }

    @Override
    public PageResult<ChapterVO> adminListChapters(Long bookId, Integer pageNum, Integer pageSize) {
        return listChapters(bookId, pageNum, pageSize, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChapter(ChapterSaveDTO dto) {
        validateBookExists(dto.getBookId());
        checkChapterNoUnique(dto.getBookId(), dto.getChapterNo(), null);

        Chapter chapter = new Chapter();
        copyChapterFields(dto, chapter);
        chapter.setWordCount(calcWordCount(dto.getContent()));
        if (dto.getPublishStatus() == 1) {
            chapter.setPublishedAt(LocalDateTime.now());
        }
        chapterMapper.insert(chapter);
        refreshBookStats(dto.getBookId());
        return chapter.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChapter(Long id, ChapterSaveDTO dto) {
        Chapter chapter = getChapterOrThrow(id);
        checkChapterNoUnique(dto.getBookId(), dto.getChapterNo(), id);
        copyChapterFields(dto, chapter);
        chapter.setWordCount(calcWordCount(dto.getContent()));
        if (dto.getPublishStatus() == 1 && chapter.getPublishedAt() == null) {
            chapter.setPublishedAt(LocalDateTime.now());
        }
        chapterMapper.updateById(chapter);
        refreshBookStats(chapter.getBookId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long id) {
        Chapter chapter = getChapterOrThrow(id);
        Long bookId = chapter.getBookId();
        chapterMapper.deleteById(id);
        refreshBookStats(bookId);
    }

    @Override
    public void refreshBookStats(Long bookId) {
        List<Chapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, bookId)
                .eq(Chapter::getPublishStatus, 1));
        Book book = bookMapper.selectById(bookId);
        if (book != null) {
            book.setTotalChapters(chapters.size());
            book.setTotalWords(chapters.stream().mapToLong(c -> c.getWordCount() != null ? c.getWordCount() : 0).sum());
            bookMapper.updateById(book);
        }
    }

    private Long getProgressChapterId(Long userId, Long bookId) {
        ReadingProgress progress = readingProgressMapper.selectOne(new LambdaQueryWrapper<ReadingProgress>()
                .eq(ReadingProgress::getUserId, userId)
                .eq(ReadingProgress::getBookId, bookId));
        return progress != null ? progress.getChapterId() : null;
    }

    private void validateBookExists(Long bookId) {
        if (bookMapper.selectById(bookId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图书不存在");
        }
    }

    private void checkChapterNoUnique(Long bookId, Integer chapterNo, Long excludeId) {
        LambdaQueryWrapper<Chapter> wrapper = new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, bookId)
                .eq(Chapter::getChapterNo, chapterNo);
        if (excludeId != null) {
            wrapper.ne(Chapter::getId, excludeId);
        }
        if (chapterMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("同一本书中章节序号不能重复");
        }
    }

    private int calcWordCount(String content) {
        if (content == null) {
            return 0;
        }
        return content.replaceAll("\\s+", "").length();
    }

    private void copyChapterFields(ChapterSaveDTO dto, Chapter chapter) {
        chapter.setBookId(dto.getBookId());
        chapter.setChapterNo(dto.getChapterNo());
        chapter.setChapterTitle(dto.getChapterTitle());
        chapter.setContent(dto.getContent());
        chapter.setIsFree(dto.getIsFree());
        chapter.setPublishStatus(dto.getPublishStatus());
    }

    private Chapter getChapterOrThrow(Long id) {
        Chapter chapter = chapterMapper.selectById(id);
        if (chapter == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "章节不存在");
        }
        return chapter;
    }

    private ChapterVO toChapterVO(Chapter chapter, boolean includeContent) {
        ChapterVO vo = new ChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        if (!includeContent) {
            vo.setContent(null);
        }
        return vo;
    }
}
