package com.example.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reading.common.PageResult;
import com.example.reading.common.ResultCode;
import com.example.reading.entity.Book;
import com.example.reading.entity.Bookshelf;
import com.example.reading.entity.Chapter;
import com.example.reading.entity.ReadingProgress;
import com.example.reading.exception.BusinessException;
import com.example.reading.mapper.BookMapper;
import com.example.reading.mapper.BookshelfMapper;
import com.example.reading.mapper.ChapterMapper;
import com.example.reading.mapper.ReadingProgressMapper;
import com.example.reading.service.BookshelfService;
import com.example.reading.vo.BookshelfVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookshelfServiceImpl implements BookshelfService {

    private final BookshelfMapper bookshelfMapper;
    private final BookMapper bookMapper;
    private final ReadingProgressMapper readingProgressMapper;
    private final ChapterMapper chapterMapper;

    @Override
    public PageResult<BookshelfVO> listBookshelf(Long userId, Integer pageNum, Integer pageSize) {
        Page<Bookshelf> page = new Page<>(pageNum, pageSize);
        Page<Bookshelf> result = bookshelfMapper.selectPage(page, new LambdaQueryWrapper<Bookshelf>()
                .eq(Bookshelf::getUserId, userId)
                .orderByDesc(Bookshelf::getLastReadAt));

        List<BookshelfVO> records = result.getRecords().stream().map(item -> {
            BookshelfVO vo = new BookshelfVO();
            vo.setId(item.getId());
            vo.setBookId(item.getBookId());
            vo.setReadingStatus(item.getReadingStatus());
            vo.setLastReadAt(item.getLastReadAt());

            Book book = bookMapper.selectById(item.getBookId());
            if (book != null) {
                vo.setTitle(book.getTitle());
                vo.setAuthor(book.getAuthor());
                vo.setCoverUrl(book.getCoverUrl());
            }

            ReadingProgress progress = readingProgressMapper.selectOne(new LambdaQueryWrapper<ReadingProgress>()
                    .eq(ReadingProgress::getUserId, userId)
                    .eq(ReadingProgress::getBookId, item.getBookId()));
            if (progress != null) {
                vo.setProgressPercent(progress.getProgressPercent());
                vo.setCurrentChapterId(progress.getChapterId());
                Chapter chapter = chapterMapper.selectById(progress.getChapterId());
                if (chapter != null) {
                    vo.setCurrentChapterTitle(chapter.getChapterTitle());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToBookshelf(Long userId, Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getPublishStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图书不存在或已下架");
        }
        Long count = bookshelfMapper.selectCount(new LambdaQueryWrapper<Bookshelf>()
                .eq(Bookshelf::getUserId, userId)
                .eq(Bookshelf::getBookId, bookId));
        if (count > 0) {
            throw new BusinessException("该图书已在书架中");
        }

        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setUserId(userId);
        bookshelf.setBookId(bookId);
        bookshelf.setReadingStatus(0);
        bookshelfMapper.insert(bookshelf);

        // 更新图书收藏量
        book.setFavoriteCount(book.getFavoriteCount() + 1);
        bookMapper.updateById(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromBookshelf(Long userId, Long bookId) {
        Bookshelf bookshelf = bookshelfMapper.selectOne(new LambdaQueryWrapper<Bookshelf>()
                .eq(Bookshelf::getUserId, userId)
                .eq(Bookshelf::getBookId, bookId));
        if (bookshelf == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "书架中不存在该图书");
        }
        bookshelfMapper.deleteById(bookshelf.getId());

        Book book = bookMapper.selectById(bookId);
        if (book != null && book.getFavoriteCount() > 0) {
            book.setFavoriteCount(book.getFavoriteCount() - 1);
            bookMapper.updateById(book);
        }
    }

    @Override
    public void updateReadingStatus(Long userId, Long bookId, Integer readingStatus) {
        Bookshelf bookshelf = bookshelfMapper.selectOne(new LambdaQueryWrapper<Bookshelf>()
                .eq(Bookshelf::getUserId, userId)
                .eq(Bookshelf::getBookId, bookId));
        if (bookshelf == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "书架中不存在该图书");
        }
        bookshelf.setReadingStatus(readingStatus);
        bookshelfMapper.updateById(bookshelf);
    }

    @Override
    public boolean isInBookshelf(Long userId, Long bookId) {
        return bookshelfMapper.selectCount(new LambdaQueryWrapper<Bookshelf>()
                .eq(Bookshelf::getUserId, userId)
                .eq(Bookshelf::getBookId, bookId)) > 0;
    }
}
