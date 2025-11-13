package crudjava.crudjava.mapper;

import crudjava.crudjava.dto.ProductDTO;
import crudjava.crudjava.model.Product;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductMapper {

    public static ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .category(product.getCategory())
            .stockQuantity(product.getStockQuantity())
            .sku(product.getSku())
            .weightKg(product.getWeightKg())
            .status(product.getStatus())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .version(product.getVersion())
            .build();
    }
}
