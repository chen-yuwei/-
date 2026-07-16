package com.example.reading.controller;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.security.SecurityUtils;
import com.example.reading.service.BookshelfService;
import com.example.reading.vo.BookshelfVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "书架接口")
@RestController
@RequestMapping("/api/bookshelf")
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;

    @Operation(summary = "我的书架")
    @GetMapping
    public Result<PageResult<BookshelfVO>> listBookshelf(@RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(bookshelfService.listBookshelf(SecurityUtils.getCurrentUserId(), pageNum, pageSize));
    }

    @Operation(summary = "加入书架")
    @PostMapping("/{bookId}")
    public Result<Void> addToBookshelf(@PathVariable Long bookId) {
        bookshelfService.addToBookshelf(SecurityUtils.getCurrentUserId(), bookId);
        return Result.success();
    }

    @Operation(summary = "移出书架")
    @DeleteMapping("/{bookId}")
    public Result<Void> removeFromBookshelf(@PathVariable Long bookId) {
        bookshelfService.removeFromBookshelf(SecurityUtils.getCurrentUserId(), bookId);
        return Result.success();
    }

    @Operation(summary = "更新阅读状态")
    @PutMapping("/{bookId}/status")
    public Result<Void> updateReadingStatus(@PathVariable Long bookId, @RequestParam Integer readingStatus) {
        bookshelfService.updateReadingStatus(SecurityUtils.getCurrentUserId(), bookId, readingStatus);
        return Result.success();
    }

    @Operation(summary = "检查是否在书架")
    @GetMapping("/check/{bookId}")
    public Result<Boolean> checkBookshelf(@PathVariable Long bookId) {
        return Result.success(bookshelfService.isInBookshelf(SecurityUtils.getCurrentUserId(), bookId));
    }
}
