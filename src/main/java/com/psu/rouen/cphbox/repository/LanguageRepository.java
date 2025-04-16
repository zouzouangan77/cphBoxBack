package com.psu.rouen.cphbox.repository;

import com.psu.rouen.cphbox.domain.Language;
import com.psu.rouen.cphbox.domain.User;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface LanguageRepository extends MongoRepository<Language, String> {
    List<Language> findByNameContainingIgnoreCase(String name);
}
