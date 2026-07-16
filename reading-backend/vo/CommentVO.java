package com.example.reading.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Long bookId;
    private Long parentId;
    private String content;
    private Integer score;
    private Integer likeCount;
    private Integer status;
    private Integer replyCount;
    private LocalDateTime createdAt;
    private List<CommentVO> replies;
}
