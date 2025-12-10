package base.api.dto.request;

import lombok.Data;

@Data
public class SyncProductRequest {
    private Long category_id;
    private Double price;
    private Long product_id;
    private String product_name;
    private String product_string;
}