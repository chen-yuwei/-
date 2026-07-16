package com.example.reading.service;

import com.example.reading.dto.ReadingProgressUpdateDTO;
import com.example.reading.vo.ReadingProgressVO;

public interface ReadingProgressService {

    ReadingProgressVO getProgress(Long userId, Long bookId);

    void saveOrUpdateProgress(Long userId, ReadingProgressUpdateDTO dto);
}
