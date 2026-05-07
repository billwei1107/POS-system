/**
 * @file ProductCategoryRepository.java
 * @description 商品分類資料存取層 / Product category repository
 */
package com.enterprise.product.repository;

import com.enterprise.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    List<ProductCategory> findByParentIdIsNullAndActiveTrueOrderBySortOrderAsc();

    List<ProductCategory> findByParentIdAndActiveTrueOrderBySortOrderAsc(UUID parentId);

    List<ProductCategory> findByActiveTrueOrderBySortOrderAsc();
}
