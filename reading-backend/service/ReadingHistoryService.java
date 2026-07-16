package com.example.reading.service;

import com.example.reading.common.PageResult;
import com.example.reading.dto.ReadingHistoryCreateDTO;
import com.example.reading.vo.ReadingHistoryVO;

public interface ReadingHistoryService {

    PageResult<ReadingHistoryVO> listHistory(Long userId, Integer pageNum, Integer pageSize);

    void addHistory(Long userId, ReadingHistoryCreateDTO dto);

    void deleteHistory(Long userId, Long id);

    void clearHistory(Long userId);
}
