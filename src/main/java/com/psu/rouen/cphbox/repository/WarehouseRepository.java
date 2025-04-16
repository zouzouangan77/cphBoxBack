package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.User;
import com.psu.rouen.cphbox.domain.Warehouse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface WarehouseRepository extends MongoRepository<Warehouse, String> {
    List<Warehouse> findByNameContainingIgnoreCase(String name);

    Optional<Warehouse> findOneByNameIgnoreCase(String name);
}
