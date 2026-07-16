package com.example.reading.controller;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.dto.CommentCreateDTO;
import com.example.reading.security.SecurityUtils;
import com.example.reading.service.CommentService;
import com.example.reading.vo.CommentVO;
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

@Tag(name = "评论接口")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "图书评论列表")
    @GetMapping("/api/books/{bookId}/comments")
    public Result<PageResult<CommentVO>> listComments(@PathVariable Long bookId,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(commentService.listBookComments(bookId, pageNum, pageSize));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/api/comments")
    public Result<Void> createComment(@Valid @RequestBody CommentCreateDTO dto) {
        commentService.createComment(SecurityUtils.getCurrentUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/api/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(SecurityUtils.getCurrentUserId(), id, false);
        return Result.success();
    }
}
