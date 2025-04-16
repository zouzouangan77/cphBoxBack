package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "box")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false,exclude = "boxCatalogs")
public class Box extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Indexed
    private String reference;

    @DocumentReference
    private Position position;

    @DocumentReference
    private Warehouse warehouse;

    // @Builder.Default
    @Field("box_catalogs")
    @DocumentReference(collection = "box_catalog")
    @ToString.Exclude
    private Set<BoxCatalog> boxCatalogs;



}
