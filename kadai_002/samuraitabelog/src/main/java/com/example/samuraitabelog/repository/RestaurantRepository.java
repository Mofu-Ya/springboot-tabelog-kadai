package com.example.samuraitabelog.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.samuraitabelog.entity.Category;
import com.example.samuraitabelog.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer>, JpaSpecificationExecutor<Restaurant>  {
	public Page<Restaurant> findByNameLike(String keyword, Pageable pageable);
	public List<Restaurant> findTop5ByOrderByCreatedAtDesc();
	public Page<Restaurant> findByNameLikeOrderByHighestPriceDesc(String nameKeyword, Pageable pageable);
	public Page<Restaurant> findByNameLikeOrderByLowestPriceAsc(String nameKeyword, Pageable pageable);
	public Page<Restaurant> findByNameLikeOrderByCreatedAtDesc(String nameKeyword, Pageable pageable);
	public Page<Restaurant> findByCategoryOrderByHighestPriceDesc(Category category, Pageable pageable);
	public Page<Restaurant> findByCategoryOrderByLowestPriceAsc(Category category, Pageable pageable);
	public Page<Restaurant> findByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);
	public Page<Restaurant> findByHighestPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable);
	public Page<Restaurant> findByHighestPriceLessThanEqualOrderByLowestPriceAsc(Integer price, Pageable pageable);
	public Page<Restaurant> findByHighestPriceLessThanEqualOrderByHighestPriceDesc(Integer price, Pageable pageable);
	
	
	public Page<Restaurant> findAllByOrderByHighestPriceDesc(Pageable pageable);
	public Page<Restaurant> findAllByOrderByLowestPriceAsc(Pageable pageable);
	public Page<Restaurant> findAllByOrderByCreatedAtDesc(Pageable pageable);
	
	
    @Query(value = "SELECT r FROM Restaurant r " +
            "WHERE (:maxPrice IS NULL OR r.lowestPrice <= :maxPrice) " +
            "AND (:name IS NULL OR r.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:categoryId IS NULL OR r.category.id = :categoryId) " +
            "ORDER BY " +
            "CASE WHEN :sortOrder = 'createdDesc' THEN r.createdAt END DESC, " +
            "CASE WHEN :sortOrder = 'priceAsc' THEN r.lowestPrice END ASC, " +
            "CASE WHEN :sortOrder = 'priceDesc' THEN r.highestPrice END DESC")
    Page<Restaurant> searchRestaurantWithCondition(
        @Param("name") String name,
        @Param("categoryId") Integer categoryId,
        @Param("maxPrice") Integer maxPrice,
        @Param("sortOrder") String sortOrder,
        Pageable pageable
    );
	

}
