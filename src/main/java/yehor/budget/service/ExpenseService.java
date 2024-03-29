package yehor.budget.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yehor.budget.common.date.DateManager;
import yehor.budget.common.exception.ObjectNotFoundException;
import yehor.budget.entity.Expense;
import yehor.budget.repository.CategoryRepository;
import yehor.budget.repository.ExpenseRepository;
import yehor.budget.repository.TagRepository;
import yehor.budget.web.converter.ExpenseConverter;
import yehor.budget.web.dto.ExpensesByTagDto;
import yehor.budget.web.dto.full.ExpenseFullDto;
import yehor.budget.web.dto.limited.ExpenseLimitedDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseConverter expenseConverter;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final DateManager dateManager;

    public BigDecimal findSumInInterval(LocalDate dateFrom, LocalDate dateTo) {
        return expenseRepository.findSumInInterval(dateFrom, dateTo);
    }

    public BigDecimal findSumInIntervalByCategory(LocalDate dateFrom, LocalDate dateTo, Long categoryId) {
        validateCategoryWithIdExists(categoryId);
        return expenseRepository.findSumInIntervalByCategory(dateFrom, dateTo, categoryId);
    }

    public List<ExpenseFullDto> findAllInDateByCategory(LocalDate date, Long categoryId) {
        validateCategoryWithIdExists(categoryId);
        List<Expense> expenses =  expenseRepository.findAllInDateByCategory(date, categoryId);
        return expenses.stream()
                .map(expenseConverter::convert)
                .toList();
    }

    public List<ExpenseFullDto> findAllInInterval(LocalDate dateFrom, LocalDate dateTo) {
        List<Expense> expenses = expenseRepository.findAllInInterval(dateFrom, dateTo);
        return expenses.stream()
                .map(expenseConverter::convert)
                .toList();
    }

    @Transactional
    public ExpenseFullDto save(ExpenseLimitedDto expenseDto) {
        validateCategoryWithIdExists(expenseDto.getCategoryId());
        validateTagsWithIdsExist(expenseDto.getTagIds());

        Expense expense = expenseConverter.convert(expenseDto);
        Expense savedExpense = expenseRepository.save(expense);
        log.info("Saved: {}", savedExpense);
        dateManager.updateBudgetDatesIfNecessary(savedExpense.getDate());
        return expenseConverter.convert(savedExpense);
    }

    @Transactional(readOnly = true)
    public ExpenseFullDto getById(Long id) {
        Expense expense = expenseRepository.getById(id);
        return expenseConverter.convert(expense);
    }

    @Transactional
    public ExpenseFullDto update(Long id, ExpenseLimitedDto expenseDto) {
        validateExists(id);
        validateCategoryWithIdExists(expenseDto.getCategoryId());
        validateTagsWithIdsExist(expenseDto.getTagIds());

        Expense expense = expenseConverter.convert(expenseDto);
        expense.setId(id);
        Expense savedExpense = expenseRepository.save(expense);
        log.info("Updated: {}", savedExpense);
        dateManager.updateBudgetDatesIfNecessary(savedExpense.getDate());
        return expenseConverter.convert(savedExpense);
    }

    public void deleteById(Long id) {
        validateExists(id);
        expenseRepository.deleteById(id);
        log.info("Expense with id {} is deleted", id);
    }

    @Transactional(readOnly = true)
    public ExpensesByTagDto getExpensesByTagId(Long id) {
        validateTagsWithIdsExist(Set.of(id));
        Set<Expense> expenses = Optional.ofNullable(
                tagRepository.getById(id).getExpenses())
                .orElse(emptySet());
        List<ExpenseFullDto> expenseDtos = expenses.stream()
                .map(expenseConverter::convert)
                .toList();
        BigDecimal total = expenseDtos.stream()
                .map(ExpenseFullDto::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ExpensesByTagDto.builder()
                .expenses(expenseDtos)
                .total(total)
                .build();
    }

    private void validateExists(Long id) {
        if (Objects.isNull(id) || !expenseRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format("Expense with id %s does not exist", id));
        }
    }

    private void validateCategoryWithIdExists(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format("Category with id %s does not exist", id));
        }
    }

    private void validateTagsWithIdsExist(Set<Long> tagIds) {
        for (Long tagId : tagIds) {
            if (!tagRepository.existsById(tagId)) {
                throw new ObjectNotFoundException(String.format("Tag with id %s does not exist", tagId));
            }
        }
    }
}
