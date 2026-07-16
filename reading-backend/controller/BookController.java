package com.example.reading.controller;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.dto.BookQueryDTO;
import com.example.reading.security.SecurityUtils;
import com.example.reading.service.BookService;
import com.example.reading.vo.BookVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "图书接口")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "图书分页列表")
    @GetMapping
    public Result<PageResult<BookVO>> listBooks(BookQueryDTO query) {
        return Result.success(bookService.listBooks(query, true));
    }

    @Operation(summary = "图书详情")
    @GetMapping("/{id}")
    public Result<BookVO> getBookDetail(@PathVariable Long id) {
        return Result.success(bookService.getBookDetail(id, SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "推荐图书")
    @GetMapping("/recommended")
    public Result<List<BookVO>> getRecommendedBooks(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(bookService.getRecommendedBooks(limit));
    }

    @Operation(summary = "热门图书")
    @GetMapping("/hot")
    public Result<List<BookVO>> getHotBooks(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(bookService.getHotBooks(limit));
    }

    @Operation(summary = "最新图书")
    @GetMapping("/latest")
    public Result<List<BookVO>> getLatestBooks(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(bookService.getLatestBooks(limit));
    }

    @Operation(summary = "搜索图书")
    @GetMapping("/search")
    public Result<PageResult<BookVO>> searchBooks(BookQueryDTO query) {
        return Result.success(bookService.searchBooks(query));
    }
}
