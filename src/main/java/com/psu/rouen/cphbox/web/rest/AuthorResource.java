package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.repository.AuthorRepository;
import com.psu.rouen.cphbox.repository.LanguageRepository;
import com.psu.rouen.cphbox.service.dto.AuthorDTO;
import com.psu.rouen.cphbox.service.dto.LanguageDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class AuthorResource {

    private final AuthorRepository authorRepository;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public AuthorResource(AuthorRepository authorRepositoryRepository) {
        this.authorRepository = authorRepositoryRepository;
    }

    @GetMapping("/authors")
    public List<AuthorDTO> getAllAuthors() {
        log.debug("REST request to get all authors");
        return authorRepository.findAll().stream().map(AuthorDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/authors/_search/{name}")
    public List<AuthorDTO> getAllAuthorsByName(@PathVariable String name) {
        log.debug("REST request to get all authors");
        return authorRepository.findByNameContainingIgnoreCase(name).stream().map(AuthorDTO::new).collect(Collectors.toList());
    }
}
