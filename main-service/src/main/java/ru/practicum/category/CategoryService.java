package ru.practicum.category;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto addNewCategory(NewCategoryDto dto);

    void deleteCategoryById(Long catId);

    CategoryDto updateCategory(Long catId, NewCategoryDto dto);

    List<CategoryDto> getCategories(Pageable pageable);

    CategoryDto getCategory(Long catId);
}
