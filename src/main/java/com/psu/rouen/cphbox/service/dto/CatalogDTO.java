package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.Catalog;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A DTO representing a user, with only the public attributes.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CatalogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String book;

    private String comment;

    private Double price;

    private LanguageDTO language;

    private AuthorDTO author;

    public CatalogDTO(Catalog catalog) {
        this.id = catalog.getId();
        this.book = catalog.getBook();
        this.comment = catalog.getComment();
        this.price = catalog.getPrice();
        this.language = new LanguageDTO(catalog.getLanguage());
        this.author = new AuthorDTO(catalog.getAuthor());
    }


    public Catalog dtoToEntity() {
        return Catalog
            .builder()
            .id(this.id)
            .book(this.book)
            .comment(this.comment)
            .price(this.price)
            .language(this.language != null ? this.language.dtoToEntity() : null)
            .author(this.getAuthor() != null ? this.getAuthor().dtoToEntity() : null)
            .build();
    }
}
