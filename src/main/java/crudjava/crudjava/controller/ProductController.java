package crudjava.crudjava.controller;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import crudjava.crudjava.dto.ProductDTO;
import crudjava.crudjava.model.Product;
import crudjava.crudjava.service.InventoryService;
import crudjava.crudjava.service.ProductService;
import crudjava.crudjava.util.UrlUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody Product product) {
        logger.info("Creating new product with SKU: {}", product.getSku());
        Product savedProduct = productService.createProduct(product);
        ProductDTO productDTO = new ProductDTO(savedProduct);
        logger.info("Successfully created product with ID: {}", savedProduct.getId());
        return new ResponseEntity<>(productDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return productService.findById(id)
                .map(product -> ResponseEntity.ok(new ProductDTO(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(Pageable pageable) {
        Page<Product> products = productService.findAll(pageable);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::new);
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        String decodedSku = UrlUtils.autoDecodeIfNeeded(sku);
        logger.info("Searching product by SKU: '{}' (decoded: '{}')", sku, decodedSku);
        return productService.findBySku(decodedSku)
                .map(product -> {
                    logger.info("Found product with SKU: {}", product.getSku());
                    return ResponseEntity.ok(new ProductDTO(product));
                })
                .orElseGet(() -> {
                    logger.warn("Product not found with SKU: {}", decodedSku);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.findByCategory(category);
        List<ProductDTO> productDTOs = products.stream().map(ProductDTO::new).toList();
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductDTO>> getProductsByStatus(@PathVariable String status) {
        List<Product> products = productService.findByStatus(status);
        List<ProductDTO> productDTOs = products.stream().map(ProductDTO::new).toList();
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String name, Pageable pageable) {
        String decodedName = UrlUtils.autoDecodeIfNeeded(name);
        logger.info("Searching products by name: '{}' (decoded: '{}')", name, decodedName);
        Page<Product> products = productService.findByNameContainingAndActive(decodedName, pageable);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::new);
        logger.info("Found {} products matching name search", products.getTotalElements());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDTO>> filterProducts(
            @RequestParam String category,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            Pageable pageable) {
        Page<Product> products = productService.findByCategoryAndPriceRange(
                category, minPrice, maxPrice, pageable);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::new);
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        List<Product> products = inventoryService.getLowStockProducts();
        List<ProductDTO> productDTOs = products.stream().map(ProductDTO::new).toList();
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/best-selling")
    public ResponseEntity<List<ProductDTO>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = inventoryService.getBestSellingProducts(limit);
        List<ProductDTO> productDTOs = products.stream().map(ProductDTO::new).toList();
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = inventoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/stats/avg-price-by-category")
    public ResponseEntity<List<Object[]>> getAveragePriceByCategory() {
        List<Object[]> stats = inventoryService.getAveragePriceByCategory();
        return ResponseEntity.ok(stats);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            ProductDTO productDTO = new ProductDTO(updatedProduct);
            return ResponseEntity.ok(productDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductDTO> updateProductStatus(
            @PathVariable Long id, @RequestParam String status) {
        try {
            Product updatedProduct = productService.updateProductStatus(id, status);
            ProductDTO productDTO = new ProductDTO(updatedProduct);
            return ResponseEntity.ok(productDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam(defaultValue = "Manual update") String reason) {
        try {
            inventoryService.updateStock(id, quantity, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
