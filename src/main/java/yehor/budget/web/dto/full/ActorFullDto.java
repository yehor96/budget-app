package yehor.budget.web.dto.full;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActorFullDto {
    private Long id;
    private String name;
}
