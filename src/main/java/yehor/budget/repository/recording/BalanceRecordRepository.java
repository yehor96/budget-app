package yehor.budget.repository.recording;

import org.springframework.data.jpa.repository.JpaRepository;
import yehor.budget.entity.recording.BalanceRecord;

public interface BalanceRecordRepository extends JpaRepository<BalanceRecord, Long> {
}