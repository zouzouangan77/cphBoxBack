package com.psu.rouen.cphbox.config.dbmigrations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psu.rouen.cphbox.domain.Author;
import com.psu.rouen.cphbox.domain.Catalog;
import com.psu.rouen.cphbox.domain.Language;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "catalogs-initialization", order = "002")
@Slf4j
public class LoadCatalog20230518 {

    private final MongoTemplate template;

    private ObjectMapper objectMapper = new ObjectMapper();

    public LoadCatalog20230518(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        List<Author> authors = loadAuthor();
        List<Language> languages = loadLanguage();
        template.insertAll(authors);
        template.insertAll(languages);

        List<Catalog> catalogs = loadCatalog();
        template.insertAll(catalogs);
    }

    @RollbackExecution
    public void rollback() {}

    private List<Author> loadAuthor() {
        try {
            return objectMapper.readValue(
                new File("src/main/resources/loadData/20230518_author.json"),
                new TypeReference<List<Author>>() {}
            );
        } catch (IOException e) {
            log.error("error load loadData/20230518_author.json", e);
        }
        return null;
    }

    private List<Language> loadLanguage() {
        try {
            return objectMapper.readValue(
                new File("src/main/resources/loadData/20230518_language.json"),
                new TypeReference<List<Language>>() {}
            );
        } catch (IOException e) {
            log.error("error load loadData/20230518_language.json", e);
        }
        return null;
    }

    private List<Catalog> loadCatalog() {
        try {
            return objectMapper.readValue(
                new File("src/main/resources/loadData/20230518_catalog.json"),
                new TypeReference<List<Catalog>>() {}
            );
        } catch (IOException e) {
            log.error("error load loadData/20230518_catalog.json", e);
        }
        return null;
    }
}
