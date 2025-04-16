package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "ware_house")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class Warehouse extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String name;

    private String address;

    @Builder.Default
    @Field("positions")
    @DocumentReference(collection = "position", lazy = true)
    @ToString.Exclude
    private Set<Position> positions = new HashSet<>();
}
