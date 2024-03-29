package yehor.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yehor.budget.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByName(@Param("name") String name);
}
