package yehor.budget.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import yehor.budget.common.date.DateManager;
import yehor.budget.common.exception.ObjectNotFoundException;
import yehor.budget.service.ExpenseService;
import yehor.budget.web.dto.ExpensesByTagDto;
import yehor.budget.web.dto.full.CategoryFullDto;
import yehor.budget.web.dto.full.ExpenseFullDto;
import yehor.budget.web.dto.full.TagFullDto;
import yehor.budget.web.dto.limited.ExpenseLimitedDto;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Controller")
public class ExpenseController {

    private final DateManager dateManager;
    private final ExpenseService expenseService;

    @GetMapping
    @Operation(summary = "Get expense by id")
    public ResponseEntity<ExpenseFullDto> getExpense(@RequestParam("id") Long id) {
        try {
            ExpenseFullDto expenseDto = expenseService.getById(id);
            return new ResponseEntity<>(expenseDto, HttpStatus.OK);
        } catch (EntityNotFoundException exception) {
            throw new ResponseStatusException(NOT_FOUND, "Expense with id " + id + " not found");
        }
    }

    @PostMapping
    @Operation(summary = "Save expense")
    public ResponseEntity<ExpenseFullDto> saveExpense(@RequestBody ExpenseLimitedDto expenseDto) {
        try {
            dateManager.validateDateAfterStart(expenseDto.getDate());
            validateValue(expenseDto.getValue());
            validateCategoryId(expenseDto.getCategoryId());
            validateTagIds(expenseDto);
            validateNote(expenseDto.getNote());

            ExpenseFullDto saved = expenseService.save(expenseDto);
            return new ResponseEntity<>(saved, HttpStatus.OK);
        } catch (ObjectNotFoundException exception) {
            throw new ResponseStatusException(NOT_FOUND, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense by id")
    public ResponseEntity<ExpenseFullDto> updateExpense(@PathVariable Long id,
                                                        @RequestBody ExpenseLimitedDto expenseDto) {
        try {
            dateManager.validateDateAfterStart(expenseDto.getDate());
            validateValue(expenseDto.getValue());
            validateCategoryId(expenseDto.getCategoryId());
            validateTagIds(expenseDto);
            validateNote(expenseDto.getNote());

            ExpenseFullDto updated = expenseService.update(id, expenseDto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (ObjectNotFoundException exception) {
            throw new ResponseStatusException(NOT_FOUND, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/interval")
    @Operation(summary = "Get list of expenses within dates interval")
    public ResponseEntity<List<ExpenseFullDto>> getExpensesInInterval(@RequestParam("dateFrom") String dateFromParam,
                                                                      @RequestParam("dateTo") String dateToParam) {
        try {
            LocalDate dateFrom = dateManager.parse(dateFromParam);
            LocalDate dateTo = dateManager.parse(dateToParam);

            dateManager.validateDatesInSequentialOrder(dateFrom, dateTo);
            dateManager.validateDatesWithinBudget(dateFrom, dateTo);

            List<ExpenseFullDto> expenseDtoList = expenseService.findAllInInterval(dateFrom, dateTo);
            return new ResponseEntity<>(expenseDtoList, HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get list of expenses for one month")
    public ResponseEntity<List<ExpenseFullDto>> getMonthlyExpenses(@RequestParam("month") Month month,
                                                                   @RequestParam("year") Integer year) {
        try {
            LocalDate dateFrom = LocalDate.of(year, month, 1);
            LocalDate dateTo = dateManager.getLastDateOfMonth(dateFrom);

            dateManager.validateDatesWithinBudget(dateFrom, dateTo);

            List<ExpenseFullDto> expenseDtoList = expenseService.findAllInInterval(dateFrom, dateTo);
            return new ResponseEntity<>(expenseDtoList, HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/sum")
    @Operation(summary = "Get sum of expenses within dates interval")
    public ResponseEntity<BigDecimal> getExpensesSumInInterval(@RequestParam("dateFrom") String dateFromParam,
                                                               @RequestParam("dateTo") String dateToParam) {
        try {
            LocalDate dateFrom = dateManager.parse(dateFromParam);
            LocalDate dateTo = dateManager.parse(dateToParam);

            dateManager.validateDatesInSequentialOrder(dateFrom, dateTo);
            dateManager.validateDatesWithinBudget(dateFrom, dateTo);

            BigDecimal sum = expenseService.findSumInInterval(dateFrom, dateTo);
            return new ResponseEntity<>(sum, HttpStatus.OK);

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/monthly/category/{categoryId}")
    @Operation(summary = "Get sum of expenses for one month by category")
    public ResponseEntity<BigDecimal> getMonthlyExpensesSumByCategory(@RequestParam("month") Month month,
                                                                      @RequestParam("year") Integer year,
                                                                      @PathVariable Long categoryId) {
        try {
            LocalDate dateFrom = LocalDate.of(year, month, 1);
            LocalDate dateTo = dateManager.getLastDateOfMonth(dateFrom);
            dateManager.validateDatesWithinBudget(dateFrom, dateTo);

            BigDecimal sum = expenseService.findSumInIntervalByCategory(dateFrom, dateTo, categoryId);
            return new ResponseEntity<>(sum, HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (ObjectNotFoundException exception) {
            throw new ResponseStatusException(NOT_FOUND, exception.getMessage());
        }
    }

    @GetMapping("/daily/category/{categoryId}")
    @Operation(summary = "Get expenses for one day by category")
    public ResponseEntity<List<ExpenseFullDto>> getDailyExpensesByCategory(@RequestParam("date") String dateParam,
                                                                           @PathVariable Long categoryId) {
        try {
            LocalDate date = dateManager.parse(dateParam);
            dateManager.validateDateWithinBudget(date);

            List<ExpenseFullDto> expenses = expenseService.findAllInDateByCategory(date, categoryId);
            return new ResponseEntity<>(expenses, HttpStatus.OK);

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        } catch (ObjectNotFoundException exception) {
            throw new ResponseStatusException(NOT_FOUND, exception.getMessage());
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete expense by id")
    public ResponseEntity<ExpenseFullDto> deleteExpense(@RequestParam("id") Long id) {
        try {
            expenseService.deleteById(id);
        } catch (ObjectNotFoundException exception) {
            throw new ResponseStatusException(NOT_FOUND, exception.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/tag")
    @Operation(summary = "Get expenses by tag id")
    public ExpensesByTagDto getExpensesByTagId(@RequestParam("tagId") Long tagId) {
        try {
            validateTagIds(Set.of(tagId));
            return expenseService.getExpensesByTagId(tagId);
        } catch (ObjectNotFoundException exception) {
            throw new ResponseStatusException(NOT_FOUND, exception.getMessage());
        }
    }

    private void validateValue(BigDecimal value) {
        if (value.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Value cannot be negative or zero!");
        }
    }

    private void validateCategoryId(Long categoryId) {
        if (Objects.isNull(categoryId) || categoryId < 1) {
            throw new IllegalArgumentException("Provided category id is not valid - " + categoryId + ". " +
                    "Please provide valid category id");
        }
    }

    private void validateTagIds(ExpenseLimitedDto expenseDto) {
        Set<Long> tagIds = expenseDto.getTagIds();
        if (Objects.isNull(tagIds)) {
            tagIds = Collections.emptySet();
            expenseDto.setTagIds(tagIds);
        } else {
            validateTagIds(tagIds);
        }
    }

    private void validateTagIds(Set<Long> tagIds) {
        if (tagIds.stream().anyMatch(id -> id < 1)) {
            throw new IllegalArgumentException("Tag cannot be negative or 0: " + tagIds);
        }
    }

    private void validateNote(String note) {
        if (Objects.nonNull(note) && note.length() > 255) {
            throw new IllegalArgumentException("Note should not be longer than 255 characters");
        }
    }

}
