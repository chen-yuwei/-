package com.example.reading.service;

import com.example.reading.common.PageResult;
import com.example.reading.dto.AdminCommentQueryDTO;
import com.example.reading.dto.CommentCreateDTO;
import com.example.reading.vo.CommentVO;

public interface CommentService {

    PageResult<CommentVO> listBookComments(Long bookId, Integer pageNum, Integer pageSize);

    void createComment(Long userId, CommentCreateDTO dto);

    void deleteComment(Long userId, Long commentId, boolean isAdmin);

    PageResult<CommentVO> adminListComments(AdminCommentQueryDTO query);

    void updateCommentStatus(Long id, Integer status);

    void adminDeleteComment(Long id);

    void refreshBookScore(Long bookId);
}
