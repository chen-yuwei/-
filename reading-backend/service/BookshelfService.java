package com.example.reading.service;

import com.example.reading.common.PageResult;
import com.example.reading.vo.BookshelfVO;

public interface BookshelfService {

    PageResult<BookshelfVO> listBookshelf(Long userId, Integer pageNum, Integer pageSize);

    void addToBookshelf(Long userId, Long bookId);

    void removeFromBookshelf(Long userId, Long bookId);

    void updateReadingStatus(Long userId, Long bookId, Integer readingStatus);

    boolean isInBookshelf(Long userId, Long bookId);
}
