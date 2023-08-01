package com.productsdata.core.controller;

import com.google.gson.Gson;
import com.productsdata.core.entity.ApprovalQueue;
import com.productsdata.core.entity.Product;
import com.productsdata.core.model.MessageResponse;
import com.productsdata.core.repository.ApprovalQueueRepository;
import com.productsdata.core.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebMvcTest(ProductController.class)
@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApprovalQueueRepository approvalQueueRepository;

    @InjectMocks
    private ProductController productController;


    private MockMvc mockMvc;
    private Gson gson = new Gson();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    public void testGetActiveProducts_NoActiveProducts() throws Exception {
        when(productRepository.getAllActiveProductList()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().json(gson.toJson(new MessageResponse("No active record found"))));
    }

    @Test
    public void testGetActiveProducts_WithActiveProducts() throws Exception {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Product1", BigDecimal.valueOf(100), "ACTIVE", new Date()));
        productList.add(new Product("Product2", BigDecimal.valueOf(200), "ACTIVE", new Date()));

        when(productRepository.getAllActiveProductList()).thenReturn(productList);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().json(gson.toJson(new MessageResponse(gson.toJson(productList)))));
    }

    @Test
    public void testSearchRequiredProducts() {
        // Mocking productRepository searchProducts method
        List<Product> productList = new ArrayList<>();
        Product product1 = new Product("Product1", new BigDecimal("100"), "In Stock", new Date());
        Product product2 = new Product("Product2", new BigDecimal("200"), "Out of Stock", new Date());
        productList.add(product1);
        productList.add(product2);

        when(productRepository.searchProducts(anyString(), any(), any(), any(), any())).thenReturn(productList);

        // Test searchRequiredProducts method
        ResponseEntity<List<Product>> response = productController.searchRequiredProducts("Product1", null, null, null, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("Product1", response.getBody().get(0).getName());
        assertEquals("Product2", response.getBody().get(1).getName());
    }

    @Test
    public void testGetAllProductsInApprovalQueue_WithNonEmptyList() {
        // Given
        List<ApprovalQueue> approvalList = new ArrayList<>();
        approvalList.add(new ApprovalQueue());

        when(approvalQueueRepository.getListByDate()).thenReturn(approvalList);

        // When
        ResponseEntity<MessageResponse> responseEntity = productController.getAllProductsInApprovalQueue();

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testDeleteProduct_Success() throws Exception {
        Long productId = 1L;

        // Mock the product in the database
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("1000"));

        // Mock the findById method of productRepository
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Mock the addToApprovalQueue method of approvalQueueRepository
        when(approvalQueueRepository.save(any())).thenReturn(new ApprovalQueue());

        // Perform the delete request
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/products/" + productId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\"message\":\"Product deletion sent for approval.\"}"));
    }

    @Test
    public void testDeleteProduct_ProductNotFound() throws Exception {
        Long productId = 1L;

        // Mock the findById method of productRepository to return an empty Optional (product not found)
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Perform the delete request
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/products/" + productId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().json("{\"message\":\"No Product Found with given id.\"}"));
    }
}