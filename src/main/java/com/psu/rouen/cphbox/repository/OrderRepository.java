package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.Event;
import com.psu.rouen.cphbox.domain.Order;
import com.psu.rouen.cphbox.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findOneByReference(String reference);
    Optional<Order> findFirstByEvent(Event event);
    Optional<Order> findFirstByUser(User user);
    Optional<Order> findOneByStatus(String status);

    List<Order> findAllByEvent(Event event);
    List<Order> findAllByUser(User user);
    List<Order> findAllByStatus(String status);

}
