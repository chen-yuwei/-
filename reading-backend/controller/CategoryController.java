package com.example.reading.controller;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.dto.BookQueryDTO;
import com.example.reading.service.BookService;
import com.example.reading.vo.BookVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "分类图书接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final BookService bookService;
    private final com.example.reading.service.CategoryService categoryService;

    @Operation(summary = "获取分类列表")
    @GetMapping
    public Result<java.util.List<com.example.reading.vo.CategoryVO>> listCategories() {
        return Result.success(categoryService.listEnabledCategories());
    }

    @Operation(summary = "分类下的图书")
    @GetMapping("/{categoryId}/books")
    public Result<PageResult<BookVO>> getBooksByCategory(@PathVariable Long categoryId, BookQueryDTO query) {
        return Result.success(bookService.getBooksByCategory(categoryId, query));
    }
}
