package base.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CreateCategoryDto {
    private Long id;
    private String name;
    private String description;
    public Long parentId;
    public Boolean isPublic = true;
    private Long userId;
}