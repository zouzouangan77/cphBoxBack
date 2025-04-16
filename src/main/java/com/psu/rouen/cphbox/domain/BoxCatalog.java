package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "box_catalog")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false,exclude = "boxStocks")
public class BoxCatalog extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @DocumentReference
    private Box box;

    @DocumentReference
    private Catalog catalog;

    @Builder.Default
    @Field("box_stocks")
    @DocumentReference(collection = "box_stock")
    @ToString.Exclude
    private Set<BoxStock> boxStocks = new HashSet<>();


}
