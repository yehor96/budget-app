package yehor.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yehor.budget.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT SUM(e.value) FROM Expense e WHERE e.date BETWEEN :dateFrom AND :dateTo")
    BigDecimal findSumInInterval(@Param("dateFrom") LocalDate dateFrom,
                                 @Param("dateTo") LocalDate dateTo);

    @Query("SELECT SUM(e.value) FROM Expense e WHERE e.category.id = :categoryId AND e.date BETWEEN :dateFrom AND :dateTo")
    BigDecimal findSumInIntervalByCategory(@Param("dateFrom") LocalDate dateFrom,
                                           @Param("dateTo") LocalDate dateTo,
                                           @Param("categoryId") Long categoryId);

    @Query("SELECT e FROM Expense e WHERE e.category.id = :categoryId AND e.date = :date")
    List<Expense> findAllInDateByCategory(@Param("date") LocalDate date,
                                          @Param("categoryId") Long categoryId);

    @Query("SELECT e FROM Expense e WHERE e.date BETWEEN :dateFrom AND :dateTo")
    List<Expense> findAllInInterval(@Param("dateFrom") LocalDate dateFrom,
                                    @Param("dateTo") LocalDate dateTo);

    @Query("SELECT e FROM Expense e WHERE e.date BETWEEN :dateFrom AND :dateTo AND e.isRegular = true")
    List<Expense> findAllRegularInInterval(@Param("dateFrom") LocalDate dateFrom,
                                           @Param("dateTo") LocalDate dateTo);
}
