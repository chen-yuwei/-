package com.example.reading.controller.admin;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.dto.BookQueryDTO;
import com.example.reading.dto.BookSaveDTO;
import com.example.reading.service.BookService;
import com.example.reading.vo.BookVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端-图书管理")
@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;

    @Operation(summary = "图书分页列表")
    @GetMapping
    public Result<PageResult<BookVO>> listBooks(BookQueryDTO query) {
        return Result.success(bookService.adminListBooks(query));
    }

    @Operation(summary = "图书详情")
    @GetMapping("/{id}")
    public Result<BookVO> getBookDetail(@PathVariable Long id) {
        return Result.success(bookService.adminGetBookDetail(id));
    }

    @Operation(summary = "添加图书")
    @PostMapping
    public Result<Long> createBook(@Valid @RequestBody BookSaveDTO dto) {
        return Result.success(bookService.createBook(dto));
    }

    @Operation(summary = "修改图书")
    @PutMapping("/{id}")
    public Result<Void> updateBook(@PathVariable Long id, @Valid @RequestBody BookSaveDTO dto) {
        bookService.updateBook(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除图书（下架）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @Operation(summary = "上架/下架图书")
    @PutMapping("/{id}/publish-status")
    public Result<Void> updatePublishStatus(@PathVariable Long id, @RequestParam Integer publishStatus) {
        bookService.updatePublishStatus(id, publishStatus);
        return Result.success();
    }

    @Operation(summary = "设置/取消推荐")
    @PutMapping("/{id}/recommended")
    public Result<Void> updateRecommended(@PathVariable Long id, @RequestParam Integer isRecommended) {
        bookService.updateRecommended(id, isRecommended);
        return Result.success();
    }
}
