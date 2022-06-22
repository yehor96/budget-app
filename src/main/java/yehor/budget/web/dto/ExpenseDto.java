package yehor.budget.web.dto;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpenseDto {

    @Hidden
    private Long id;
    private BigDecimal value;
    private LocalDate date;
    private Boolean isRegular;
    private Long categoryId;
}