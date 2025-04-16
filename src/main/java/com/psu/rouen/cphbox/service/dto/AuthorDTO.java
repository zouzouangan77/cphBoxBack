package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.Author;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AuthorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    public AuthorDTO(Author author) {
        if (author == null) {
            return;
        }
        this.id = author.getId();
        this.name = author.getName();
    }

    public Author dtoToEntity() {
        return Author.builder().id(this.id).name(this.name).build();
    }
}
