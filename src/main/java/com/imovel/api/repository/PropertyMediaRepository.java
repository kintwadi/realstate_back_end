package com.imovel.api.repository;

import com.imovel.api.model.PropertyMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyMediaRepository extends JpaRepository<PropertyMedia, String> {

    List<PropertyMedia> findAllByPropertyId(Long propertyId);
    Optional<PropertyMedia> findByName(String name);
    Optional<PropertyMedia> findByIdAndPropertyId(String id, Long propertyId);
    void deleteByName(String fileName);
    List<PropertyMedia> findAllByNameStartingWith(String prefix);
}
