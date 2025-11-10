package base.api.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double salePrice;
    private int totalDays;
}