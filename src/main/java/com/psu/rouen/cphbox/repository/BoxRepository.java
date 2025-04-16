package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.*;

import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface BoxRepository extends MongoRepository<Box, String> {
    Optional<Box> findOneByReference(String reference);

    Optional<Box> findFirstByWarehouse(Warehouse warehouse);

    Optional<Box> findFirstByPosition(Position position);


    List<Box> findAllByWarehouse(Warehouse warehouse);


}
