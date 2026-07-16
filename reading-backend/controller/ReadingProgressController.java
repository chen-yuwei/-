package com.example.reading.controller;

import com.example.reading.common.Result;
import com.example.reading.dto.ReadingProgressUpdateDTO;
import com.example.reading.security.SecurityUtils;
import com.example.reading.service.ReadingProgressService;
import com.example.reading.vo.ReadingProgressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "阅读进度接口")
@RestController
@RequestMapping("/api/reading-progress")
@RequiredArgsConstructor
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;

    @Operation(summary = "获取阅读进度")
    @GetMapping("/{bookId}")
    public Result<ReadingProgressVO> getProgress(@PathVariable Long bookId) {
        return Result.success(readingProgressService.getProgress(SecurityUtils.getCurrentUserId(), bookId));
    }

    @Operation(summary = "保存阅读进度")
    @PutMapping
    public Result<Void> saveProgress(@Valid @RequestBody ReadingProgressUpdateDTO dto) {
        readingProgressService.saveOrUpdateProgress(SecurityUtils.getCurrentUserId(), dto);
        return Result.success();
    }
}
