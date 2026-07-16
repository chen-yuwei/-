package com.example.reading.service;

import com.example.reading.common.PageResult;
import com.example.reading.dto.BookQueryDTO;
import com.example.reading.dto.BookSaveDTO;
import com.example.reading.vo.BookVO;

import java.util.List;

public interface BookService {

    PageResult<BookVO> listBooks(BookQueryDTO query, boolean onlyPublished);

    BookVO getBookDetail(Long id, Long userId);

    List<BookVO> getRecommendedBooks(int limit);

    List<BookVO> getHotBooks(int limit);

    List<BookVO> getLatestBooks(int limit);

    PageResult<BookVO> searchBooks(BookQueryDTO query);

    PageResult<BookVO> getBooksByCategory(Long categoryId, BookQueryDTO query);

    PageResult<BookVO> adminListBooks(BookQueryDTO query);

    BookVO adminGetBookDetail(Long id);

    Long createBook(BookSaveDTO dto);

    void updateBook(Long id, BookSaveDTO dto);

    void deleteBook(Long id);

    void updatePublishStatus(Long id, Integer publishStatus);

    void updateRecommended(Long id, Integer isRecommended);

    void incrementViewCount(Long bookId, Long userId, String clientKey);
}
