package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "order_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false, exclude = "order")
public class OrderItem extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @DocumentReference
    private Order order;

    @DocumentReference
    private Box box;

    private Integer quantity;

    private Boolean orderItemRetrieved;

    private Integer orderItemQuantityInitialWanted;

    private Boolean isSelected;


    @Field("box_catalog")
    private BoxCatalog boxCatalog;

    private String status;
    private String comment;
}
