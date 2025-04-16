package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.Author;
import com.psu.rouen.cphbox.domain.Language;
import com.psu.rouen.cphbox.domain.User;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface AuthorRepository extends MongoRepository<Author, String> {
    List<Author> findByNameContainingIgnoreCase(String name);
}
