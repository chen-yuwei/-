package com.example.reading.service;

import com.example.reading.dto.CategorySaveDTO;
import com.example.reading.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> listEnabledCategories();

    List<CategoryVO> listAllCategories();

    Long createCategory(CategorySaveDTO dto);

    void updateCategory(Long id, CategorySaveDTO dto);

    void deleteCategory(Long id);
}
