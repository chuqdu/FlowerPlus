package base.api.dto.request;

import lombok.Data;

@Data
public class SyncProductRequest {
    private Long category_id;
    private Double price;
    private Long product_id;
    private String product_name;
    private String product_string;

    @Override
    public String toString() {
        return String.format("{\"category_id\": %s, \"price\": %.0f, \"product_id\": %d, \"product_name\": \"%s\", \"product_string\": \"%s\"}", 
            category_id, price, product_id, product_name, 
            product_string != null ? product_string.substring(0, Math.min(100, product_string.length())) + "..." : "null");
    }
}