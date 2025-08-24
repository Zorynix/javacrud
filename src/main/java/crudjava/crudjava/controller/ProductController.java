package crudjava.crudjava.controller;

import java.math.BigDecimal;
import java.util.List;

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

import crudjava.crudjava.dto.CreateProductRequestDTO;
import crudjava.crudjava.dto.ProductDTO;
import crudjava.crudjava.service.InventoryService;
import crudjava.crudjava.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequestDTO request) {
        ProductDTO productDTO = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return productService.findById(id)
                .map(productDTO -> ResponseEntity.ok(productDTO))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(Pageable pageable) {
        Page<ProductDTO> products = productService.findAll(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        return productService.findBySku(sku)
                .map(productDTO -> ResponseEntity.ok(productDTO))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String category) {
        List<ProductDTO> products = productService.findByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductDTO>> getProductsByStatus(@PathVariable String status) {
        List<ProductDTO> products = productService.findByStatus(status);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String name, Pageable pageable) {
        Page<ProductDTO> products = productService.findByNameContainingAndActive(name, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDTO>> filterProducts(
            @RequestParam String category,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            Pageable pageable) {
        Page<ProductDTO> products = productService.findByCategoryAndPriceRange(
                category, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        List<ProductDTO> products = inventoryService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/best-selling")
    public ResponseEntity<List<ProductDTO>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductDTO> products = inventoryService.getBestSellingProducts(limit);
        return ResponseEntity.ok(products);
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
            @PathVariable Long id, @Valid @RequestBody CreateProductRequestDTO request) {
        ProductDTO productDTO = productService.updateProduct(id, request);
        return ResponseEntity.ok(productDTO);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductDTO> updateProductStatus(
            @PathVariable Long id, @RequestParam String status) {
        ProductDTO productDTO = productService.updateProductStatus(id, status);
        return ResponseEntity.ok(productDTO);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam(defaultValue = "Manual update") String reason) {
        inventoryService.updateStock(id, quantity, reason);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
