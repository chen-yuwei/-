package com.example.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reading.common.ResultCode;
import com.example.reading.dto.CategorySaveDTO;
import com.example.reading.entity.BookCategory;
import com.example.reading.entity.Category;
import com.example.reading.exception.BusinessException;
import com.example.reading.mapper.BookCategoryMapper;
import com.example.reading.mapper.CategoryMapper;
import com.example.reading.service.CategoryService;
import com.example.reading.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final BookCategoryMapper bookCategoryMapper;

    @Override
    public List<CategoryVO> listEnabledCategories() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
        return buildTree(categories);
    }

    @Override
    public List<CategoryVO> listAllCategories() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder));
        return buildTree(categories);
    }

    @Override
    public Long createCategory(CategorySaveDTO dto) {
        checkCodeUnique(dto.getCategoryCode(), null);
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateCategory(Long id, CategorySaveDTO dto) {
        Category category = getCategoryOrThrow(id);
        checkCodeUnique(dto.getCategoryCode(), id);
        BeanUtils.copyProperties(dto, category);
        category.setId(id);
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        getCategoryOrThrow(id);
        Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该分类下存在子分类，无法删除");
        }
        Long bookCount = bookCategoryMapper.selectCount(new LambdaQueryWrapper<BookCategory>()
                .eq(BookCategory::getCategoryId, id));
        if (bookCount > 0) {
            throw new BusinessException("该分类下存在图书，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private void checkCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryCode, code);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类编码已存在");
        }
    }

    private Category getCategoryOrThrow(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    private List<CategoryVO> buildTree(List<Category> categories) {
        List<CategoryVO> vos = categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());

        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : vos) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                vo.setChildren(findChildren(vo.getId(), vos));
                roots.add(vo);
            }
        }
        return roots;
    }

    private List<CategoryVO> findChildren(Long parentId, List<CategoryVO> all) {
        List<CategoryVO> children = new ArrayList<>();
        for (CategoryVO vo : all) {
            if (parentId.equals(vo.getParentId())) {
                vo.setChildren(findChildren(vo.getId(), all));
                children.add(vo);
            }
        }
        return children.isEmpty() ? null : children;
    }
}
