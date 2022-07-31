package common.factory;

import lombok.experimental.UtilityClass;
import yehor.budget.web.dto.full.ExpenseFullDto;
import yehor.budget.web.dto.limited.ExpenseLimitedDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static common.factory.CategoryFactory.DEFAULT_CATEGORY_ID;

@UtilityClass
public class ExpenseFactory {

    public static final long DEFAULT_EXPENSE_ID = 1L;

    public static ExpenseFullDto defaultExpenseFullDto() {
        return ExpenseFullDto.builder()
                .id(DEFAULT_EXPENSE_ID)
                .value(new BigDecimal("10.00"))
                .date(LocalDate.now())
                .isRegular(true)
                .categoryId(DEFAULT_CATEGORY_ID)
                .build();
    }

    public static ExpenseFullDto secondExpenseFullDto() {
        return ExpenseFullDto.builder()
                .id(2L)
                .value(new BigDecimal("100.00"))
                .date(LocalDate.now().minusDays(1))
                .isRegular(false)
                .categoryId(DEFAULT_CATEGORY_ID)
                .build();
    }

    public static ExpenseFullDto thirdExpenseFullDto() {
        return ExpenseFullDto.builder()
                .id(3L)
                .value(new BigDecimal("15.50"))
                .date(LocalDate.now().minusDays(2))
                .isRegular(false)
                .categoryId(DEFAULT_CATEGORY_ID)
                .build();
    }

    public static List<ExpenseFullDto> defaultExpenseFullDtoList() {
        ExpenseFullDto expenseFullDto1 = defaultExpenseFullDto();
        ExpenseFullDto expenseFullDto2 = secondExpenseFullDto();
        ExpenseFullDto expenseFullDto3 = thirdExpenseFullDto();
        return List.of(expenseFullDto1, expenseFullDto2, expenseFullDto3);
    }

    public static ExpenseLimitedDto defaultExpenseLimitedDto() {
        return ExpenseLimitedDto.builder()
                .value(new BigDecimal("10.00"))
                .date(LocalDate.now())
                .isRegular(true)
                .categoryId(DEFAULT_CATEGORY_ID)
                .build();
    }

    public static ExpenseLimitedDto secondExpenseLimitedDto() {
        return ExpenseLimitedDto.builder()
                .value(new BigDecimal("100.00"))
                .date(LocalDate.now().minusDays(1))
                .isRegular(false)
                .categoryId(DEFAULT_CATEGORY_ID)
                .build();
    }

    public static ExpenseLimitedDto thirdExpenseLimitedDto() {
        return ExpenseLimitedDto.builder()
                .value(new BigDecimal("15.50"))
                .date(LocalDate.now().minusDays(2))
                .isRegular(false)
                .categoryId(DEFAULT_CATEGORY_ID)
                .build();
    }



}