package base.api.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryNodeDto {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    public boolean isPublic;
    private List<CategoryNodeDto> children = new ArrayList<>();
}
