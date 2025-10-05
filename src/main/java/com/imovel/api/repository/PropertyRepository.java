package com.imovel.api.repository;

import com.imovel.api.model.Property;
import com.imovel.api.model.enums.PropertyCategory;
import com.imovel.api.model.enums.PropertyStatus;
import com.imovel.api.model.enums.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query(value = "SELECT p FROM Property p ORDER BY p.id")
    List<Property> findAllPropertiesWithPagination();

    @Query("SELECT COUNT(p) FROM Property p")
    long countAllProperties();

    @Query("SELECT COUNT(p) FROM Property p WHERE " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:price IS NULL OR p.price = :price)")
    long countPropertiesWithFilter(
            @Param("type") PropertyType type,
            @Param("category") PropertyCategory category,
            @Param("status") PropertyStatus status,
            @Param("price") BigDecimal price);

    @Query("SELECT p FROM Property p WHERE " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:price IS NULL OR p.price = :price) " +
            "ORDER BY p.id")
    List<Property> findPropertiesWithFilter(
            @Param("type") PropertyType type,
            @Param("category") PropertyCategory category,
            @Param("status") PropertyStatus status,
            @Param("price") BigDecimal price);
}
