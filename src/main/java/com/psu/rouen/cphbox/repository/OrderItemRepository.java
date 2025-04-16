package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.OrderItem;
import com.psu.rouen.cphbox.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface OrderItemRepository extends MongoRepository<OrderItem, String> {}
