package crudjava.crudjava.service;

import crudjava.crudjava.dto.CreateProductRequestDTO;
import crudjava.crudjava.dto.ProductDTO;
import crudjava.crudjava.exception.ProductNotFoundException;
import crudjava.crudjava.mapper.ProductMapper;
import crudjava.crudjava.model.Product;
import crudjava.crudjava.repository.ProductRepository;
import crudjava.crudjava.util.UrlUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(CreateProductRequestDTO request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        String sku = request.getSku();
        if (sku == null || sku.isEmpty()) {
            sku = generateSku(request.getCategory());
        }

        if (productRepository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException(
                "Product with SKU " + sku + " already exists"
            );
        }

        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .sku(sku)
            .price(request.getPrice())
            .category(request.getCategory())
            .status(
                request.getStatus() != null ? request.getStatus() : "ACTIVE"
            )
            .stockQuantity(
                request.getStockQuantity() != null
                    ? request.getStockQuantity()
                    : 0
            )
            .build();

        Product savedProduct = productRepository.save(product);
        log.info(
            "Successfully created product with ID: {}",
            savedProduct.getId()
        );

        return ProductMapper.toDTO(savedProduct);
    }

    @CacheEvict(value = "products", key = "#id")
    public ProductDTO updateProduct(Long id, CreateProductRequestDTO request) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository
            .findById(id)
            .orElseThrow(() ->
                new ProductNotFoundException("Product not found: " + id)
            );

        if (
            request.getSku() != null &&
            !existingProduct.getSku().equals(request.getSku())
        ) {
            if (productRepository.findBySku(request.getSku()).isPresent()) {
                throw new IllegalArgumentException(
                    "Product with SKU " + request.getSku() + " already exists"
                );
            }
            existingProduct.setSku(request.getSku());
        }

        if (request.getName() != null) existingProduct.setName(
            request.getName()
        );
        if (request.getDescription() != null) existingProduct.setDescription(
            request.getDescription()
        );
        if (request.getPrice() != null) existingProduct.setPrice(
            request.getPrice()
        );
        if (request.getCategory() != null) existingProduct.setCategory(
            request.getCategory()
        );
        if (request.getStatus() != null) existingProduct.setStatus(
            request.getStatus()
        );
        if (
            request.getStockQuantity() != null
        ) existingProduct.setStockQuantity(request.getStockQuantity());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Successfully updated product with ID: {}", id);

        return ProductMapper.toDTO(updatedProduct);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public Optional<ProductDTO> findById(Long id) {
        return productRepository
            .findById(id)
            .map(product -> {
                log.info("Found product with ID: {}", id);
                return ProductMapper.toDTO(product);
            });
    }

    @Transactional(readOnly = true)
    public Optional<ProductDTO> findBySku(String sku) {
        String decodedSku = UrlUtils.autoDecodeIfNeeded(sku);
        log.info("Searching product by SKU: {}", decodedSku);
        return productRepository
            .findBySku(decodedSku)
            .map(product -> {
                log.info("Found product with SKU: {}", decodedSku);
                return ProductMapper.toDTO(product);
            });
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByCategory(String category) {
        log.info("Finding products by category: {}", category);
        List<Product> products = productRepository.findByCategory(category);
        return products.stream().map(ProductMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByStatus(String status) {
        log.info("Finding products by status: {}", status);
        List<Product> products = productRepository.findByStatus(status);
        return products.stream().map(ProductMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findByNameContainingAndActive(
        String name,
        Pageable pageable
    ) {
        String decodedName = UrlUtils.autoDecodeIfNeeded(name);
        log.info("Searching products by name: {}", decodedName);
        Page<Product> products =
            productRepository.findByNameContainingAndActive(
                decodedName,
                pageable
            );
        log.info(
            "Found {} products matching name search",
            products.getTotalElements()
        );
        return products.map(ProductMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategoryAndPriceRange(
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Pageable pageable
    ) {
        log.info(
            "Finding products by category {} and price range {} - {}",
            category,
            minPrice,
            maxPrice
        );
        Page<Product> products = productRepository.findByCategoryAndPriceRange(
            category,
            minPrice,
            maxPrice,
            pageable
        );
        return products.map(ProductMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Pageable pageable) {
        log.info(
            "Fetching all products, page: {}, size: {}",
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        Page<Product> products = productRepository.findAll(pageable);
        log.info(
            "Retrieved {} products out of {} total",
            products.getNumberOfElements(),
            products.getTotalElements()
        );
        return products.map(ProductMapper::toDTO);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        Product product = productRepository
            .findById(id)
            .orElseThrow(() ->
                new ProductNotFoundException("Product not found: " + id)
            );

        if (
            product.getOrderItems() != null &&
            !product.getOrderItems().isEmpty()
        ) {
            product.setStatus("DISCONTINUED");
            productRepository.save(product);
            log.info("Marked product as discontinued: {}", id);
        } else {
            productRepository.delete(product);
            log.info("Successfully deleted product: {}", id);
        }
    }

    @CacheEvict(value = "products", key = "#productId")
    public ProductDTO updateProductStatus(Long productId, String status) {
        log.info("Updating product {} status to {}", productId, status);
        Product product = productRepository
            .findById(productId)
            .orElseThrow(() ->
                new ProductNotFoundException("Product not found: " + productId)
            );

        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);
        log.info(
            "Successfully updated product {} status to {}",
            productId,
            status
        );

        return ProductMapper.toDTO(updatedProduct);
    }

    private String generateSku(String category) {
        String prefix = category != null && category.length() >= 3
            ? category.substring(0, 3).toUpperCase()
            : "PRD";
        String suffix = UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
        return prefix + "-" + suffix;
    }
}
