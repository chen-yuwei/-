package com.example.reading.controller.admin;

import com.example.reading.common.PageResult;
import com.example.reading.common.Result;
import com.example.reading.dto.AdminCommentQueryDTO;
import com.example.reading.service.CommentService;
import com.example.reading.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端-评论管理")
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @Operation(summary = "评论分页列表")
    @GetMapping
    public Result<PageResult<CommentVO>> listComments(AdminCommentQueryDTO query) {
        return Result.success(commentService.adminListComments(query));
    }

    @Operation(summary = "审核/屏蔽评论")
    @PutMapping("/{id}/status")
    public Result<Void> updateCommentStatus(@PathVariable Long id, @RequestParam Integer status) {
        commentService.updateCommentStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除违规评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.adminDeleteComment(id);
        return Result.success();
    }
}
