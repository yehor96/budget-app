package yehor.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import yehor.budget.entity.Category;
import yehor.budget.repository.CategoryRepository;
import yehor.budget.web.converter.CategoryConverter;
import yehor.budget.web.dto.limited.CategoryLimitedDto;
import yehor.budget.web.dto.full.CategoryFullDto;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.StreamSupport;

import static yehor.budget.exception.CategoryExceptionProvider.cannotDeleteCategoryWithDependentExpensesException;
import static yehor.budget.exception.CategoryExceptionProvider.categoryAlreadyExistsException;
import static yehor.budget.exception.CategoryExceptionProvider.categoryDoesNotExistException;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryConverter categoryConverter;

    public List<CategoryFullDto> getAll() {
        Iterable<Category> categories = categoryRepository.findAll();
        return StreamSupport.stream(categories.spliterator(), false)
                .map(categoryConverter::convert)
                .toList();
    }

    public void save(CategoryLimitedDto categoryDto) {
        Category category = categoryConverter.convert(categoryDto);
        validateCategoryDoNotExist(category);
        categoryRepository.save(category);
    }

    public void delete(Long id) {
        try {
            categoryRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw categoryDoesNotExistException(id);
        } catch (DataIntegrityViolationException e) {
            throw cannotDeleteCategoryWithDependentExpensesException();
        }
    }

    @Transactional
    public void update(CategoryFullDto categoryDto) {
        validateCategoryExists(categoryDto.getId());
        Category category = categoryConverter.convert(categoryDto);
        categoryRepository.update(category);
    }

    private void validateCategoryDoNotExist(Category category) {
        categoryRepository.findByName(category.getName())
                .ifPresent(e -> {
                    throw categoryAlreadyExistsException(category.getName());
                });
    }

    private void validateCategoryExists(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw categoryDoesNotExistException(id);
        }
    }
}
