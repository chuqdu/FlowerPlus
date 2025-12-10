package base.api.dto.request;

import lombok.Data;

@Data
public class SyncCategoryRequest {
    private Long category_id;
    private String category_name;

    @Override
    public String toString() {
        return String.format("{\"category_id\": %d, \"category_name\": \"%s\"}", 
            category_id, category_name);
    }
}