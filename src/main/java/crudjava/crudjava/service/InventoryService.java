package crudjava.crudjava.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import crudjava.crudjava.config.RabbitConfig;
import crudjava.crudjava.dto.InventoryEventDto;
import crudjava.crudjava.dto.ProductDTO;
import crudjava.crudjava.model.Product;
import crudjava.crudjava.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private static final int LOW_STOCK_THRESHOLD = 10;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "reserveInventoryFallback")
    @CacheEvict(value = "products", key = "#productId")
    public boolean reserveInventory(Long productId, Integer quantity, String reason) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Product product = productOpt.get();
        Integer oldQuantity = product.getStockQuantity();

        if (oldQuantity < quantity) {
            logger.warn("Insufficient stock for product {}: requested {}, available {}",
                    product.getSku(), quantity, oldQuantity);
            return false;
        }

        int updatedRows = productRepository.decreaseStock(productId, quantity);
        if (updatedRows == 0) {
            logger.warn("Failed to reserve inventory for product {}: concurrent modification", product.getSku());
            return false;
        }

        product = productRepository.findById(productId).orElseThrow();
        Integer newQuantity = product.getStockQuantity();

        publishInventoryEvent(product, oldQuantity, newQuantity, "DECREASE", reason);

        if (newQuantity <= LOW_STOCK_THRESHOLD) {
            publishLowStockAlert(product);
        }

        logger.info("Reserved {} units of product {}: {} -> {}",
                quantity, product.getSku(), oldQuantity, newQuantity);
        return true;
    }

    public boolean reserveInventoryFallback(Long productId, Integer quantity, String reason, Exception ex) {
        logger.error("Inventory reservation failed for product {}: {}", productId, ex.getMessage());
        return false;
    }

    @CacheEvict(value = "products", key = "#productId")
    public void releaseInventory(Long productId, Integer quantity, String reason) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            logger.warn("Cannot release inventory: Product not found {}", productId);
            return;
        }

        Product product = productOpt.get();
        Integer oldQuantity = product.getStockQuantity();

        productRepository.increaseStock(productId, quantity);

        product = productRepository.findById(productId).orElseThrow();
        Integer newQuantity = product.getStockQuantity();

        publishInventoryEvent(product, oldQuantity, newQuantity, "INCREASE", reason);

        logger.info("Released {} units of product {}: {} -> {}",
                quantity, product.getSku(), oldQuantity, newQuantity);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void updateStock(Long productId, Integer newQuantity, String reason) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Product product = productOpt.get();
        Integer oldQuantity = product.getStockQuantity();

        product.setStockQuantity(newQuantity);
        productRepository.save(product);

        publishInventoryEvent(product, oldQuantity, newQuantity, "SET", reason);

        if (newQuantity <= LOW_STOCK_THRESHOLD && oldQuantity > LOW_STOCK_THRESHOLD) {
            publishLowStockAlert(product);
        }

        logger.info("Updated stock for product {}: {} -> {}", product.getSku(), oldQuantity, newQuantity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "lowStockProducts")
    public List<ProductDTO> getLowStockProducts() {
        logger.info("Finding low stock products with threshold: {}", LOW_STOCK_THRESHOLD);
        List<Product> products = productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
        return products.stream().map(ProductDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getBestSellingProducts(int limit) {
        logger.info("Finding best selling products, limit: {}", limit);
        List<Product> products = productRepository.findBestSellingProducts(limit);
        return products.stream().map(ProductDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAveragePriceByCategory() {
        return productRepository.findAveragePriceByCategory();
    }

    @Transactional(readOnly = true)
    public List<String> getAllActiveCategories() {
        return productRepository.findAllActiveCategories();
    }

    private void publishInventoryEvent(Product product, Integer oldQuantity, Integer newQuantity,
                                     String operation, String reason) {
        try {
            InventoryEventDto event = new InventoryEventDto(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    oldQuantity,
                    newQuantity,
                    operation,
                    reason,
                    LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(RabbitConfig.INVENTORY_EXCHANGE,
                    RabbitConfig.INVENTORY_UPDATE_ROUTING_KEY, event);
            logger.debug("Published inventory event for product {}: {} {} -> {}",
                    product.getSku(), operation, oldQuantity, newQuantity);
        } catch (Exception e) {
            logger.error("Failed to publish inventory event for product {}: {}",
                    product.getSku(), e.getMessage());
        }
    }

    private void publishLowStockAlert(Product product) {
        try {
            InventoryEventDto event = new InventoryEventDto(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    null,
                    product.getStockQuantity(),
                    "LOW_STOCK_ALERT",
                    "Stock quantity below threshold: " + LOW_STOCK_THRESHOLD,
                    LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(RabbitConfig.INVENTORY_EXCHANGE,
                    RabbitConfig.LOW_STOCK_ALERT_ROUTING_KEY, event);
            logger.warn("Low stock alert sent for product {}: {} units remaining",
                    product.getSku(), product.getStockQuantity());
        } catch (Exception e) {
            logger.error("Failed to send low stock alert for product {}: {}",
                    product.getSku(), e.getMessage());
        }
    }
}
