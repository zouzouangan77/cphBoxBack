package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    Optional<Event> findOneByName(String name);

    Optional<Event> findFirstByAddress(String warehouse);

    Optional<Event> findFirstByEventDate(Instant position);
    List<Event> findByNameContainingIgnoreCase(String name);

    Optional<Event> findOneByNameIgnoreCase(String name);

}
