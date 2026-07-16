package com.example.reading.controller;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.security.SecurityUtils;
import com.example.reading.service.ChapterService;
import com.example.reading.vo.ChapterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "章节接口")
@RestController
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @Operation(summary = "图书章节目录")
    @GetMapping("/api/books/{bookId}/chapters")
    public Result<PageResult<ChapterVO>> listChapters(@PathVariable Long bookId,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.success(chapterService.listChapters(bookId, pageNum, pageSize,
                SecurityUtils.getCurrentUserId(), true));
    }

    @Operation(summary = "章节详情（阅读）")
    @GetMapping("/api/chapters/{chapterId}")
    public Result<ChapterVO> getChapterDetail(@PathVariable Long chapterId, HttpServletRequest request) {
        String clientKey = request.getRemoteAddr();
        return Result.success(chapterService.getChapterDetail(chapterId,
                SecurityUtils.getCurrentUserId(), clientKey, true));
    }

    @Operation(summary = "上一章")
    @GetMapping("/api/chapters/{chapterId}/previous")
    public Result<ChapterVO> getPreviousChapter(@PathVariable Long chapterId) {
        return Result.success(chapterService.getPreviousChapter(chapterId, true));
    }

    @Operation(summary = "下一章")
    @GetMapping("/api/chapters/{chapterId}/next")
    public Result<ChapterVO> getNextChapter(@PathVariable Long chapterId) {
        return Result.success(chapterService.getNextChapter(chapterId, true));
    }
}
