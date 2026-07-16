package com.example.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reading.common.PageResult;
import com.example.reading.common.ResultCode;
import com.example.reading.dto.BookQueryDTO;
import com.example.reading.dto.BookSaveDTO;
import com.example.reading.entity.Book;
import com.example.reading.entity.BookCategory;
import com.example.reading.entity.Chapter;
import com.example.reading.exception.BusinessException;
import com.example.reading.mapper.BookCategoryMapper;
import com.example.reading.mapper.BookMapper;
import com.example.reading.mapper.ChapterMapper;
import com.example.reading.service.BookService;
import com.example.reading.service.BookshelfService;
import com.example.reading.service.CategoryService;
import com.example.reading.service.ReadingProgressService;
import com.example.reading.util.ViewCountCache;
import com.example.reading.vo.BookVO;
import com.example.reading.vo.CategoryVO;
import com.example.reading.vo.ChapterVO;
import com.example.reading.vo.ReadingProgressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookCategoryMapper bookCategoryMapper;
    private final ChapterMapper chapterMapper;
    private final CategoryService categoryService;
    private final BookshelfService bookshelfService;
    private final ReadingProgressService readingProgressService;
    private final ViewCountCache viewCountCache;

    @Override
    public PageResult<BookVO> listBooks(BookQueryDTO query, boolean onlyPublished) {
        return queryBooks(query, onlyPublished);
    }

    @Override
    public BookVO getBookDetail(Long id, Long userId) {
        Book book = getBookOrThrow(id);
        if (book.getPublishStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图书不存在或已下架");
        }
        BookVO vo = toBookVO(book);
        fillBookExtraInfo(vo, userId);
        return vo;
    }

    @Override
    public List<BookVO> getRecommendedBooks(int limit) {
        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getPublishStatus, 1)
                .eq(Book::getIsRecommended, 1)
                .orderByDesc(Book::getUpdatedAt)
                .last("LIMIT " + limit));
        return books.stream().map(this::toBookVO).collect(Collectors.toList());
    }

    @Override
    public List<BookVO> getHotBooks(int limit) {
        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getPublishStatus, 1)
                .orderByDesc(Book::getViewCount)
                .last("LIMIT " + limit));
        return books.stream().map(this::toBookVO).collect(Collectors.toList());
    }

    @Override
    public List<BookVO> getLatestBooks(int limit) {
        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getPublishStatus, 1)
                .orderByDesc(Book::getUpdatedAt)
                .last("LIMIT " + limit));
        return books.stream().map(this::toBookVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<BookVO> searchBooks(BookQueryDTO query) {
        return queryBooks(query, true);
    }

    @Override
    public PageResult<BookVO> getBooksByCategory(Long categoryId, BookQueryDTO query) {
        query.setCategoryId(categoryId);
        return queryBooks(query, true);
    }

    @Override
    public PageResult<BookVO> adminListBooks(BookQueryDTO query) {
        return queryBooks(query, false);
    }

    @Override
    public BookVO adminGetBookDetail(Long id) {
        Book book = getBookOrThrow(id);
        BookVO vo = toBookVO(book);
        vo.setCategories(categoryService.listAllCategories().stream()
                .flatMap(c -> flattenCategories(c).stream())
                .filter(c -> getBookCategoryIds(id).contains(c.getId()))
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBook(BookSaveDTO dto) {
        Book book = new Book();
        copyBookFields(dto, book);
        book.setTotalChapters(0);
        book.setTotalWords(0L);
        book.setViewCount(0L);
        book.setFavoriteCount(0);
        book.setCommentCount(0);
        book.setAverageScore(java.math.BigDecimal.ZERO);
        bookMapper.insert(book);
        saveBookCategories(book.getId(), dto.getCategoryIds());
        return book.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBook(Long id, BookSaveDTO dto) {
        Book book = getBookOrThrow(id);
        copyBookFields(dto, book);
        bookMapper.updateById(book);
        bookCategoryMapper.delete(new LambdaQueryWrapper<BookCategory>().eq(BookCategory::getBookId, id));
        saveBookCategories(id, dto.getCategoryIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        Book book = getBookOrThrow(id);
        // 逻辑删除：下架而非物理删除
        book.setPublishStatus(0);
        bookMapper.updateById(book);
    }

    @Override
    public void updatePublishStatus(Long id, Integer publishStatus) {
        Book book = getBookOrThrow(id);
        book.setPublishStatus(publishStatus);
        bookMapper.updateById(book);
    }

    @Override
    public void updateRecommended(Long id, Integer isRecommended) {
        Book book = getBookOrThrow(id);
        book.setIsRecommended(isRecommended);
        bookMapper.updateById(book);
    }

    @Override
    public void incrementViewCount(Long bookId, Long userId, String clientKey) {
        String key = bookId + ":" + (userId != null ? userId : clientKey);
        if (viewCountCache.shouldCount(key)) {
            Book book = getBookOrThrow(bookId);
            book.setViewCount(book.getViewCount() + 1);
            bookMapper.updateById(book);
        }
    }

    private PageResult<BookVO> queryBooks(BookQueryDTO query, boolean onlyPublished) {
        Page<Book> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();

        if (onlyPublished) {
            wrapper.eq(Book::getPublishStatus, 1);
        }
        if (query.getCategoryId() != null) {
            List<Long> bookIds = bookCategoryMapper.selectList(new LambdaQueryWrapper<BookCategory>()
                            .eq(BookCategory::getCategoryId, query.getCategoryId()))
                    .stream().map(BookCategory::getBookId).collect(Collectors.toList());
            if (bookIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0, query.getPageNum(), query.getPageSize());
            }
            wrapper.in(Book::getId, bookIds);
        }
        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(Book::getTitle, query.getTitle());
        }
        if (StringUtils.hasText(query.getAuthor())) {
            wrapper.like(Book::getAuthor, query.getAuthor());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Book::getTitle, query.getKeyword())
                    .or().like(Book::getAuthor, query.getKeyword()));
        }

        applySort(wrapper, query.getSortField(), query.getSortOrder());

        Page<Book> result = bookMapper.selectPage(page, wrapper);
        List<BookVO> records = result.getRecords().stream().map(this::toBookVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private void applySort(LambdaQueryWrapper<Book> wrapper, String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if ("viewCount".equals(sortField)) {
            wrapper.orderBy(true, asc, Book::getViewCount);
        } else if ("favoriteCount".equals(sortField)) {
            wrapper.orderBy(true, asc, Book::getFavoriteCount);
        } else if ("createdAt".equals(sortField)) {
            wrapper.orderBy(true, asc, Book::getCreatedAt);
        } else {
            wrapper.orderBy(true, asc, Book::getUpdatedAt);
        }
    }

    private void fillBookExtraInfo(BookVO vo, Long userId) {
        vo.setCategories(getBookCategories(vo.getId()));
        vo.setLatestChapter(getLatestChapter(vo.getId()));
        if (userId != null) {
            vo.setInBookshelf(bookshelfService.isInBookshelf(userId, vo.getId()));
            vo.setReadingProgress(readingProgressService.getProgress(userId, vo.getId()));
        } else {
            vo.setInBookshelf(false);
        }
    }

    private List<CategoryVO> getBookCategories(Long bookId) {
        List<Long> categoryIds = getBookCategoryIds(bookId);
        if (categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        return categoryService.listEnabledCategories().stream()
                .flatMap(c -> flattenCategories(c).stream())
                .filter(c -> categoryIds.contains(c.getId()))
                .collect(Collectors.toList());
    }

    private List<Long> getBookCategoryIds(Long bookId) {
        return bookCategoryMapper.selectList(new LambdaQueryWrapper<BookCategory>()
                        .eq(BookCategory::getBookId, bookId))
                .stream().map(BookCategory::getCategoryId).collect(Collectors.toList());
    }

    private List<CategoryVO> flattenCategories(CategoryVO category) {
        List<CategoryVO> list = new java.util.ArrayList<>();
        list.add(category);
        if (!CollectionUtils.isEmpty(category.getChildren())) {
            category.getChildren().forEach(child -> list.addAll(flattenCategories(child)));
        }
        return list;
    }

    private ChapterVO getLatestChapter(Long bookId) {
        Chapter chapter = chapterMapper.selectOne(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, bookId)
                .eq(Chapter::getPublishStatus, 1)
                .orderByDesc(Chapter::getChapterNo)
                .last("LIMIT 1"));
        if (chapter == null) {
            return null;
        }
        ChapterVO vo = new ChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        return vo;
    }

    private void saveBookCategories(Long bookId, List<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            throw new BusinessException("图书至少需要一个分类");
        }
        for (Long categoryId : categoryIds) {
            BookCategory bc = new BookCategory();
            bc.setBookId(bookId);
            bc.setCategoryId(categoryId);
            bookCategoryMapper.insert(bc);
        }
    }

    private void copyBookFields(BookSaveDTO dto, Book book) {
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setCoverUrl(dto.getCoverUrl());
        book.setSummary(dto.getSummary());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setSerializeStatus(dto.getSerializeStatus());
        book.setPublishStatus(dto.getPublishStatus());
        book.setIsRecommended(dto.getIsRecommended() != null ? dto.getIsRecommended() : 0);
    }

    private Book getBookOrThrow(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图书不存在");
        }
        return book;
    }

    private BookVO toBookVO(Book book) {
        BookVO vo = new BookVO();
        BeanUtils.copyProperties(book, vo);
        return vo;
    }
}
