package com.example.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reading.common.PageResult;
import com.example.reading.common.ResultCode;
import com.example.reading.dto.AdminCommentQueryDTO;
import com.example.reading.dto.CommentCreateDTO;
import com.example.reading.entity.Book;
import com.example.reading.entity.BookComment;
import com.example.reading.entity.SysUser;
import com.example.reading.exception.BusinessException;
import com.example.reading.mapper.BookCommentMapper;
import com.example.reading.mapper.BookMapper;
import com.example.reading.mapper.SysUserMapper;
import com.example.reading.service.CommentService;
import com.example.reading.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final BookCommentMapper bookCommentMapper;
    private final BookMapper bookMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<CommentVO> listBookComments(Long bookId, Integer pageNum, Integer pageSize) {
        Page<BookComment> page = new Page<>(pageNum, pageSize);
        Page<BookComment> result = bookCommentMapper.selectPage(page, new LambdaQueryWrapper<BookComment>()
                .eq(BookComment::getBookId, bookId)
                .isNull(BookComment::getParentId)
                .eq(BookComment::getStatus, 1)
                .orderByDesc(BookComment::getCreatedAt));

        List<CommentVO> records = result.getRecords().stream().map(comment -> {
            CommentVO vo = toVO(comment);
            vo.setReplyCount(countReplies(comment.getId()));
            vo.setReplies(getReplies(comment.getId()));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createComment(Long userId, CommentCreateDTO dto) {
        Book book = bookMapper.selectById(dto.getBookId());
        if (book == null || book.getPublishStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图书不存在或已下架");
        }

        if (dto.getParentId() != null) {
            BookComment parent = bookCommentMapper.selectById(dto.getParentId());
            if (parent == null || !parent.getBookId().equals(dto.getBookId())) {
                throw new BusinessException("回复的评论不存在");
            }
            dto.setScore(null);
        } else {
            if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) {
                throw new BusinessException("一级评论评分范围为1至5分");
            }
        }

        BookComment comment = new BookComment();
        comment.setUserId(userId);
        comment.setBookId(dto.getBookId());
        comment.setParentId(dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setScore(dto.getScore());
        comment.setLikeCount(0);
        comment.setStatus(1);
        bookCommentMapper.insert(comment);

        if (dto.getParentId() == null) {
            refreshBookScore(dto.getBookId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId, boolean isAdmin) {
        BookComment comment = bookCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的评论");
        }

        Long bookId = comment.getBookId();
        boolean isTopLevel = comment.getParentId() == null;

        if (isTopLevel) {
            // 删除一级评论时，同时删除其所有回复
            bookCommentMapper.delete(new LambdaQueryWrapper<BookComment>()
                    .eq(BookComment::getParentId, commentId));
        }
        bookCommentMapper.deleteById(commentId);

        if (isTopLevel) {
            refreshBookScore(bookId);
        }
    }

    @Override
    public PageResult<CommentVO> adminListComments(AdminCommentQueryDTO query) {
        Page<BookComment> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BookComment> wrapper = new LambdaQueryWrapper<BookComment>()
                .isNull(BookComment::getParentId)
                .orderByDesc(BookComment::getCreatedAt);
        if (query.getBookId() != null) {
            wrapper.eq(BookComment::getBookId, query.getBookId());
        }
        if (query.getUserId() != null) {
            wrapper.eq(BookComment::getUserId, query.getUserId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(BookComment::getStatus, query.getStatus());
        }

        Page<BookComment> result = bookCommentMapper.selectPage(page, wrapper);
        List<CommentVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public void updateCommentStatus(Long id, Integer status) {
        BookComment comment = bookCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        comment.setStatus(status);
        bookCommentMapper.updateById(comment);
        if (comment.getParentId() == null) {
            refreshBookScore(comment.getBookId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteComment(Long id) {
        deleteComment(null, id, true);
    }

    @Override
    public void refreshBookScore(Long bookId) {
        BigDecimal avgScore = bookCommentMapper.calcAverageScore(bookId);
        Integer commentCount = bookCommentMapper.countTopLevelComments(bookId);

        Book book = bookMapper.selectById(bookId);
        if (book != null) {
            book.setAverageScore(avgScore.setScale(2, RoundingMode.HALF_UP));
            book.setCommentCount(commentCount);
            bookMapper.updateById(book);
        }
    }

    private int countReplies(Long parentId) {
        return bookCommentMapper.selectCount(new LambdaQueryWrapper<BookComment>()
                .eq(BookComment::getParentId, parentId)
                .eq(BookComment::getStatus, 1)).intValue();
    }

    private List<CommentVO> getReplies(Long parentId) {
        List<BookComment> replies = bookCommentMapper.selectList(new LambdaQueryWrapper<BookComment>()
                .eq(BookComment::getParentId, parentId)
                .eq(BookComment::getStatus, 1)
                .orderByAsc(BookComment::getCreatedAt));
        return replies.stream().map(this::toVO).collect(Collectors.toList());
    }

    private CommentVO toVO(BookComment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        SysUser user = sysUserMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
        }
        return vo;
    }
}
