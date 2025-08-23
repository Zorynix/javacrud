package crudjava.crudjava.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import crudjava.crudjava.model.Product;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockQuantity;
    private String sku;
    private BigDecimal weightKg;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.category = product.getCategory();
        this.stockQuantity = product.getStockQuantity();
        this.sku = product.getSku();
        this.weightKg = product.getWeightKg();
        this.status = product.getStatus();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        this.version = product.getVersion();
    }
}
