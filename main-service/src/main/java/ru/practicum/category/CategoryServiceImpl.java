package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.ValidationException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addNewCategory(NewCategoryDto dto) {
        if (repository.existsCategoryByName(dto.getName())) {
            throw new AlreadyExistsException("Категория уже существует");
        }
        if (dto.getName().isBlank()) {
            throw new ValidationException("Имя Категории не состоит из пробелов");
        }
        return CategoryMapper.toCategoryDto(repository.save(CategoryMapper.toCategory(dto)));
    }

    @Override
    public void deleteCategoryById(Long catId) {
        if (!repository.existsById(catId)) {
            throw new DataNotFoundException("Категория с таким id не существует");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Событие еще не удалено");
        }
        repository.deleteById(catId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto dto) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new ValidationException("Название категории не может быть пустым");
        }
        Category updateCategory = repository.findById(catId).orElseThrow(() -> new DataNotFoundException("Категория не найдена"));
        if (!updateCategory.getName().equals(dto.getName()) && repository.existsCategoryByName(dto.getName())) {
            throw new AlreadyExistsException("Категория с таким именем уже существует" );
        }
        updateCategory.setName(dto.getName());
        return CategoryMapper.toCategoryDto(repository.save(updateCategory));
    }

    @Transactional
    @Override
    public List<CategoryDto> getCategories(Pageable pageable) {
        return repository.findAll(pageable).stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = repository.findById(catId).orElseThrow(() -> new DataNotFoundException("Категория не найдена"));
        return CategoryMapper.toCategoryDto(category);
    }
}
