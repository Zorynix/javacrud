package crudjava.crudjava.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import crudjava.crudjava.model.Product;

public record ProductDTO(
    Long id,
    String name,
    String description,
    BigDecimal price,
    String category,
    Integer stockQuantity,
    String sku,
    BigDecimal weightKg,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version
) {
    public ProductDTO(Product product) {
        this(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getStockQuantity(),
            product.getSku(),
            product.getWeightKg(),
            product.getStatus(),
            product.getCreatedAt(),
            product.getUpdatedAt(),
            product.getVersion()
        );
    }
}
