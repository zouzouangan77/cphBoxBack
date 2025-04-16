package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface CatalogRepository extends MongoRepository<Catalog, String> {
    List<Catalog> findByBookContainingIgnoreCase(String name);

    Optional<Catalog> findOneByBookIgnoreCase(String name);

    List<Catalog> findAllByLanguage(Language language);
}

