package base.api.dto.request;

import lombok.Data;

@Data
public class SyncCategoryRequest {
    private Long category_id;
    private String category_name;
}