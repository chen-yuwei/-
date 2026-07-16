package com.example.reading.service;

import com.example.reading.common.PageResult;
import com.example.reading.dto.ChapterSaveDTO;
import com.example.reading.vo.ChapterVO;

public interface ChapterService {

    PageResult<ChapterVO> listChapters(Long bookId, Integer pageNum, Integer pageSize, Long userId, boolean onlyPublished);

    ChapterVO getChapterDetail(Long chapterId, Long userId, String clientKey, boolean onlyPublished);

    ChapterVO getPreviousChapter(Long chapterId, boolean onlyPublished);

    ChapterVO getNextChapter(Long chapterId, boolean onlyPublished);

    PageResult<ChapterVO> adminListChapters(Long bookId, Integer pageNum, Integer pageSize);

    Long createChapter(ChapterSaveDTO dto);

    void updateChapter(Long id, ChapterSaveDTO dto);

    void deleteChapter(Long id);

    void refreshBookStats(Long bookId);
}
