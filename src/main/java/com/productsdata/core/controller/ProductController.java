package com.productsdata.core.controller;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.productsdata.core.entity.ApprovalQueue;
import com.productsdata.core.entity.Product;
import com.productsdata.core.model.MessageResponse;
import com.productsdata.core.model.ProductRequestDto;
import com.productsdata.core.repository.ApprovalQueueRepository;
import com.productsdata.core.repository.ProductRepository;

@RestController
public class ProductController {
	Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create(); 

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ApprovalQueueRepository approvalQueueRepository;

    private static final BigDecimal MAX_ALLOWED_PRICE = new BigDecimal("10000");
    private static final BigDecimal PRICE_THRESHOLD_FOR_APPROVAL = new BigDecimal("5000");

    // Get active products
    @GetMapping("/api/products")
    public ResponseEntity<MessageResponse> getActiveProducts() {
        // Fetch a list of all active products from the database
        List<Product> productList = productRepository.getAllActiveProductList();

        if (productList.isEmpty()) {
            // If no active products are found, return a message response indicating that
            return ResponseEntity.ok(new MessageResponse("No active record found"));
        } else {
            // If active products are found, return the list of products
            return ResponseEntity.ok(new MessageResponse(gson.toJson(productList)));
        }
    }

    // Search products based on criteria
    @GetMapping("/api/products/search")
    public ResponseEntity<List<Product>> searchRequiredProducts(
            // Request parameters for filtering the search
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date minDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date maxDate
    ) {
        // Perform a database query based on the provided search criteria
        List<Product> productList = productRepository.searchProducts(productName, minPrice, maxPrice, minDate, maxDate);

        if (productList.isEmpty()) {
            // If no matching products are found, return a message response indicating that
            return ResponseEntity.ok(Collections.emptyList());
        } else {
            // If matching products are found, return the list of products
            return ResponseEntity.ok(productList);
        }
    }
    
    // Create a product
    @PostMapping("/api/products")
    public ResponseEntity<MessageResponse> createProduct(@RequestBody ProductRequestDto productRequestDto) {
        // Check if the product price exceeds the maximum allowed price
        if (productRequestDto.getPrice().compareTo(MAX_ALLOWED_PRICE) > 0) {
            // If the price exceeds the limit, return a bad request with an error message
            return ResponseEntity.badRequest().body(new MessageResponse("Product price cannot exceed $10,000."));
        }

        // Create a new product object from the incoming productRequestDto
        Product product = new Product(
                productRequestDto.getName(),
                productRequestDto.getPrice(),
                productRequestDto.getStatus(),
                new Date()
        );

        if (productRequestDto.getPrice().compareTo(PRICE_THRESHOLD_FOR_APPROVAL) > 0) {
            // If the price is above the approval threshold, send the product for approval
            addToApprovalQueue(product, "add_product");
            return ResponseEntity.ok(new MessageResponse("Product sent for approval."));
        }

        // Save the product directly to the database since it does not require approval
        product = productRepository.save(product);
        return ResponseEntity.ok(new MessageResponse(gson.toJson(product)));
    }

    // Helper method to add a product to the approval queue
    private void addToApprovalQueue(Product product, String action) {
        // Create a new entry in the approval queue with the product and action information
        ApprovalQueue approvalQueueEntry = new ApprovalQueue(product, new Date(), action);
        approvalQueueRepository.save(approvalQueueEntry);
    }

    // Update a product
    @PutMapping("/api/products/{productId}")
    public ResponseEntity<MessageResponse> updateProduct(@PathVariable Long productId, @RequestBody ProductRequestDto productRequestDto) {
        // Check if the product price exceeds the maximum allowed price
        if (productRequestDto.getPrice().compareTo(MAX_ALLOWED_PRICE) > 0) {
            // If the price exceeds the limit, return a bad request with an error message
            return ResponseEntity.badRequest().body(new MessageResponse("Product price cannot exceed $10,000."));
        }

        // Find the product in the database by its ID
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            // If no product is found with the given ID, return a bad request with an error message
            return ResponseEntity.badRequest().body(new MessageResponse("No Product Found with given id."));
        }

        // Calculate the new price (increase by 50%)
        BigDecimal previousPrice = product.getPrice();
        BigDecimal newPrice = productRequestDto.getPrice().multiply(BigDecimal.valueOf(1.5));

        // If the new price is greater than or equal to the previous price, send for approval
        if (newPrice.compareTo(previousPrice) >= 0) {
            addToApprovalQueue(product, "update_product");
            return ResponseEntity.ok(new MessageResponse("Product sent for approval."));
        }

        // Update the product details and save it to the database
        product.setName(productRequestDto.getName());
        product.setPrice(productRequestDto.getPrice());
        product.setStatus(productRequestDto.getStatus());
        productRepository.save(product);

        return ResponseEntity.ok(new MessageResponse("Your product has been updated."));
    }

    // Delete a product
    @DeleteMapping("/api/products/{productId}")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable Long productId) {
        // Find the product in the database by its ID
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            // If no product is found with the given ID, return a bad request with an error message
            return ResponseEntity.badRequest().body(new MessageResponse("No Product Found with given id."));
        }

        // Add the product to the approval queue for deletion
        addToApprovalQueue(product, "delete_product");

        return ResponseEntity.ok(new MessageResponse("Product deletion sent for approval."));
    }

    // Get all products in the approval queue
    @GetMapping("/api/products/approval-queue")
    public ResponseEntity<MessageResponse> getAllProductsInApprovalQueue() {
        // Fetch a list of all products in the approval queue from the database
        List<ApprovalQueue> approvalList = approvalQueueRepository.getListByDate();

        if (approvalList.isEmpty()) {
            // If no products are found in the approval queue, return a message response indicating that
            return ResponseEntity.ok(new MessageResponse("No active record found in approval queue."));
        } else {
            // If products are found in the approval queue, return the list of products
            return ResponseEntity.ok(new MessageResponse(gson.toJson(approvalList)));
        }
    }

    // Approve or reject a product from the approval queue
    @PutMapping("/api/products/approval-queue/{approvalId}")
    public ResponseEntity<MessageResponse> approveOrRejectProduct(@PathVariable long approvalId, @RequestParam String status) {
        // Find the approval queue entry in the database by its ID
        ApprovalQueue approvalQueue = approvalQueueRepository.findById(approvalId).orElse(null);

        if (approvalQueue == null) {
            // If no approval queue entry is found with the given ID, return a bad request with an error message
            return ResponseEntity.badRequest().body(new MessageResponse("Approval queue entry not found."));
        }

        // Get the product associated with the approval queue entry
        Product product = approvalQueue.getProduct();

        if (status.equalsIgnoreCase("APPROVE")) {
            // If the status is "APPROVE", process the approval based on the action type
            String action = approvalQueue.getAction();
            approvalQueueRepository.delete(approvalQueue);

            // Process approval based on the action type
            if (action.equals("add_product") || action.equals("update_product")) {
                productRepository.save(product);
                return ResponseEntity.ok(new MessageResponse("Product updated or added."));
            } else if (action.equals("delete_product")) {
                productRepository.delete(product);
                return ResponseEntity.ok(new MessageResponse("Product deleted."));
            }
        } else {
            // If the status is "REJECT", remove the entry from the approval queue
            approvalQueueRepository.delete(approvalQueue);
            return ResponseEntity.ok(new MessageResponse("Product request rejected."));
        }

        // Return a generic response if the status is neither "APPROVE" nor "REJECT"
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid status provided."));
    }
}
