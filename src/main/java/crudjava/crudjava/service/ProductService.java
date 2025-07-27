package crudjava.crudjava.service;

import crudjava.crudjava.model.Product;
import crudjava.crudjava.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(Product product) {
        if (product.getSku() == null || product.getSku().isEmpty()) {
            product.setSku(generateSku(product.getCategory()));
        }

        if (productRepository.findBySku(product.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists");
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Created new product: {} with SKU: {}", savedProduct.getName(), savedProduct.getSku());
        return savedProduct;
    }

    @CacheEvict(value = "products", key = "#id")
    public Product updateProduct(Long id, Product productUpdates) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        if (!existingProduct.getSku().equals(productUpdates.getSku())) {
            if (productRepository.findBySku(productUpdates.getSku()).isPresent()) {
                throw new IllegalArgumentException("Product with SKU " + productUpdates.getSku() + " already exists");
            }
        }

        existingProduct.setName(productUpdates.getName());
        existingProduct.setDescription(productUpdates.getDescription());
        existingProduct.setPrice(productUpdates.getPrice());
        existingProduct.setCategory(productUpdates.getCategory());
        existingProduct.setSku(productUpdates.getSku());
        existingProduct.setWeightKg(productUpdates.getWeightKg());
        existingProduct.setStatus(productUpdates.getStatus());

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Updated product: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Transactional(readOnly = true)
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Product> findByStatus(Product.ProductStatus status) {
        return productRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByNameContainingAndActive(String name, Pageable pageable) {
        return productRepository.findByNameContainingAndActive(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByCategoryAndPriceRange(String category, BigDecimal minPrice,
                                                   BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByCategoryAndPriceRange(category, minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        if (!product.getOrderItems().isEmpty()) {
            product.setStatus(Product.ProductStatus.DISCONTINUED);
            productRepository.save(product);
            logger.info("Marked product as discontinued: {}", id);
        } else {
            productRepository.delete(product);
            logger.info("Deleted product: {}", id);
        }
    }

    @CacheEvict(value = "products", key = "#productId")
    public Product updateProductStatus(Long productId, Product.ProductStatus status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);
        logger.info("Updated product {} status to {}", productId, status);
        return updatedProduct;
    }

    private String generateSku(String category) {
        String prefix = category != null && category.length() >= 3 ?
                category.substring(0, 3).toUpperCase() : "PRD";
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + suffix;
    }
}
