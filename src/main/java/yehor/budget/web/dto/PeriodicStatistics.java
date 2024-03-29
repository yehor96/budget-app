package yehor.budget.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicStatistics {
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal avgMonthlyTotalExpense = BigDecimal.ZERO;
    private BigDecimal avgMonthlyTotalRegular = BigDecimal.ZERO;
    private BigDecimal avgMonthlyTotalNonRegular = BigDecimal.ZERO;
    private Map<String, MonthlyStatistics> monthToMonthlyStatisticsMap;
}
