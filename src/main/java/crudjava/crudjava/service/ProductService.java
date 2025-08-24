package crudjava.crudjava.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import crudjava.crudjava.dto.CreateProductRequestDTO;
import crudjava.crudjava.dto.ProductDTO;
import crudjava.crudjava.exception.ProductNotFoundException;
import crudjava.crudjava.model.Product;
import crudjava.crudjava.repository.ProductRepository;
import crudjava.crudjava.util.UrlUtils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(CreateProductRequestDTO request) {
        logger.info("Creating new product with SKU: {}", request.sku());
        
        String sku = request.sku();
        if (sku == null || sku.isEmpty()) {
            sku = generateSku(request.category());
        }

        if (productRepository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + sku + " already exists");
        }

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .sku(sku)
                .price(request.price())
                .category(request.category())
                .status(request.status() != null ? request.status() : "ACTIVE")
                .stockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0)
                .build();

        Product savedProduct = productRepository.save(product);
        logger.info("Successfully created product with ID: {}", savedProduct.getId());
        
        return new ProductDTO(savedProduct);
    }

    @CacheEvict(value = "products", key = "#id")
    public ProductDTO updateProduct(Long id, CreateProductRequestDTO request) {
        logger.info("Updating product with ID: {}", id);
        
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        if (request.sku() != null && !existingProduct.getSku().equals(request.sku())) {
            if (productRepository.findBySku(request.sku()).isPresent()) {
                throw new IllegalArgumentException("Product with SKU " + request.sku() + " already exists");
            }
            existingProduct.setSku(request.sku());
        }

        if (request.name() != null) existingProduct.setName(request.name());
        if (request.description() != null) existingProduct.setDescription(request.description());
        if (request.price() != null) existingProduct.setPrice(request.price());
        if (request.category() != null) existingProduct.setCategory(request.category());
        if (request.status() != null) existingProduct.setStatus(request.status());
        if (request.stockQuantity() != null) existingProduct.setStockQuantity(request.stockQuantity());

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Successfully updated product with ID: {}", id);
        
        return new ProductDTO(updatedProduct);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public Optional<ProductDTO> findById(Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    logger.info("Found product with ID: {}", id);
                    return new ProductDTO(product);
                });
    }

    @Transactional(readOnly = true)
    public Optional<ProductDTO> findBySku(String sku) {
        String decodedSku = UrlUtils.autoDecodeIfNeeded(sku);
        logger.info("Searching product by SKU: {}", decodedSku);
        return productRepository.findBySku(decodedSku)
                .map(product -> {
                    logger.info("Found product with SKU: {}", decodedSku);
                    return new ProductDTO(product);
                });
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByCategory(String category) {
        logger.info("Finding products by category: {}", category);
        List<Product> products = productRepository.findByCategory(category);
        return products.stream().map(ProductDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByStatus(String status) {
        logger.info("Finding products by status: {}", status);
        List<Product> products = productRepository.findByStatus(status);
        return products.stream().map(ProductDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findByNameContainingAndActive(String name, Pageable pageable) {
        String decodedName = UrlUtils.autoDecodeIfNeeded(name);
        logger.info("Searching products by name: {}", decodedName);
        Page<Product> products = productRepository.findByNameContainingAndActive(decodedName, pageable);
        logger.info("Found {} products matching name search", products.getTotalElements());
        return products.map(ProductDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategoryAndPriceRange(String category, BigDecimal minPrice,
                                                       BigDecimal maxPrice, Pageable pageable) {
        logger.info("Finding products by category {} and price range {} - {}", category, minPrice, maxPrice);
        Page<Product> products = productRepository.findByCategoryAndPriceRange(category, minPrice, maxPrice, pageable);
        return products.map(ProductDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Pageable pageable) {
        logger.info("Fetching all products, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAll(pageable);
        logger.info("Retrieved {} products out of {} total", products.getNumberOfElements(), products.getTotalElements());
        return products.map(ProductDTO::new);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        if (product.getOrderItems() != null && !product.getOrderItems().isEmpty()) {
            product.setStatus("DISCONTINUED");
            productRepository.save(product);
            logger.info("Marked product as discontinued: {}", id);
        } else {
            productRepository.delete(product);
            logger.info("Successfully deleted product: {}", id);
        }
    }

    @CacheEvict(value = "products", key = "#productId")
    public ProductDTO updateProductStatus(Long productId, String status) {
        logger.info("Updating product {} status to {}", productId, status);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);
        logger.info("Successfully updated product {} status to {}", productId, status);
        
        return new ProductDTO(updatedProduct);
    }

    private String generateSku(String category) {
        String prefix = category != null && category.length() >= 3 ?
                category.substring(0, 3).toUpperCase() : "PRD";
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + suffix;
    }
}
