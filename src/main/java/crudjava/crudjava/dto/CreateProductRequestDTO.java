package crudjava.crudjava.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequestDTO {
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String category;
    private String status;
    private Integer stockQuantity;
}
