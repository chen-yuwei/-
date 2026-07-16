package com.example.reading.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("book_category")
public class BookCategory {

    private Long bookId;
    private Long categoryId;
    private LocalDateTime createdAt;
}
