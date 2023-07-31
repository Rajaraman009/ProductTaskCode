package com.productsdata.core.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.productsdata.core.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query(value = "select * from products_data pd where pd.product_status = 'active'", nativeQuery = true)
	List<Product> getAllActiveProductList();

	@Query(value = "SELECT * FROM products_data p " + "WHERE (:productName IS NULL OR p.product_name LIKE %:productName%) "
			+ "AND (:minPrice IS NULL OR p.product_price >= :minPrice) " + "AND (:maxPrice IS NULL OR p.product_price <= :maxPrice) "
			+ "AND (:minPostedDate IS NULL OR p.posted_date >= :minPostedDate) "
			+ "AND (:maxPostedDate IS NULL OR p.posted_date <= :maxPostedDate)", nativeQuery = true)
	List<Product> searchProducts(@Param("productName") String productName, @Param("minPrice") BigDecimal minPrice,
			@Param("maxPrice") BigDecimal maxPrice, @Param("minPostedDate") Date minPostedDate,
			@Param("maxPostedDate") Date maxPostedDate);

}
