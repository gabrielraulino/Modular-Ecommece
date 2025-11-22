package com.modulith.ecommerce.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "select * from products where id in (:ids)", nativeQuery = true)
    List<Product> findAllByIdIn(Set<Long> ids);

}
