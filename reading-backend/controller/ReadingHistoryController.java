package com.example.reading.controller;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.dto.ReadingHistoryCreateDTO;
import com.example.reading.security.SecurityUtils;
import com.example.reading.service.ReadingHistoryService;
import com.example.reading.vo.ReadingHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "阅读历史接口")
@RestController
@RequestMapping("/api/reading-history")
@RequiredArgsConstructor
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    @Operation(summary = "阅读历史列表")
    @GetMapping
    public Result<PageResult<ReadingHistoryVO>> listHistory(@RequestParam(defaultValue = "1") Integer pageNum,
                                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(readingHistoryService.listHistory(SecurityUtils.getCurrentUserId(), pageNum, pageSize));
    }

    @Operation(summary = "添加阅读历史")
    @PostMapping
    public Result<Void> addHistory(@Valid @RequestBody ReadingHistoryCreateDTO dto) {
        readingHistoryService.addHistory(SecurityUtils.getCurrentUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "删除单条历史")
    @DeleteMapping("/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        readingHistoryService.deleteHistory(SecurityUtils.getCurrentUserId(), id);
        return Result.success();
    }

    @Operation(summary = "清空全部历史")
    @DeleteMapping
    public Result<Void> clearHistory() {
        readingHistoryService.clearHistory(SecurityUtils.getCurrentUserId());
        return Result.success();
    }
}
