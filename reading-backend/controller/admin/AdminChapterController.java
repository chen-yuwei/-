package com.example.reading.controller.admin;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.dto.ChapterSaveDTO;
import com.example.reading.service.ChapterService;
import com.example.reading.vo.ChapterVO;
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

@Tag(name = "管理端-章节管理")
@RestController
@RequiredArgsConstructor
public class AdminChapterController {

    private final ChapterService chapterService;

    @Operation(summary = "查询指定图书的章节")
    @GetMapping("/api/admin/books/{bookId}/chapters")
    public Result<PageResult<ChapterVO>> listChapters(@PathVariable Long bookId,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.success(chapterService.adminListChapters(bookId, pageNum, pageSize));
    }

    @Operation(summary = "添加章节")
    @PostMapping("/api/admin/chapters")
    public Result<Long> createChapter(@Valid @RequestBody ChapterSaveDTO dto) {
        return Result.success(chapterService.createChapter(dto));
    }

    @Operation(summary = "编辑章节")
    @PutMapping("/api/admin/chapters/{id}")
    public Result<Void> updateChapter(@PathVariable Long id, @Valid @RequestBody ChapterSaveDTO dto) {
        chapterService.updateChapter(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除章节")
    @DeleteMapping("/api/admin/chapters/{id}")
    public Result<Void> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return Result.success();
    }
}
