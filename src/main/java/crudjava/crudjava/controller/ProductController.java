package crudjava.crudjava.controller;

import crudjava.crudjava.dto.ProductDTO;
import crudjava.crudjava.model.Product;
import crudjava.crudjava.service.InventoryService;
import crudjava.crudjava.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        ProductDTO productDTO = new ProductDTO(savedProduct);
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
        return productService.findBySku(sku)
                .map(product -> ResponseEntity.ok(new ProductDTO(product)))
                .orElse(ResponseEntity.notFound().build());
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
        Page<Product> products = productService.findByNameContainingAndActive(name, pageable);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::new);
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
