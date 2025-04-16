package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.Box;
import com.psu.rouen.cphbox.domain.BoxCatalog;
import com.psu.rouen.cphbox.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface BoxCatalogRepository extends MongoRepository<BoxCatalog, String> { }
