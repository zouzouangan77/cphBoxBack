package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "catalog")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class Catalog extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Indexed
    private String book;

    private String comment;
    private Double price;

    @DocumentReference
    @NotNull
    private Language language;

    @DocumentReference
    @NotNull
    private Author author;
}
