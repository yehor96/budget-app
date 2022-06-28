package yehor.budget.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @SequenceGenerator(name = "expenses_sequence", sequenceName = "expenses_expense_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expenses_sequence")
    @Column(name = "expense_id")
    private Long id;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "is_regular")
    private Boolean isRegular;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
