package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "box_stock")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false,exclude = "boxCatalog")
public class BoxStock extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @DocumentReference
    @Field("box_catalog")
    private BoxCatalog boxCatalog;

    @DocumentReference
    @Field("order_item")
    private OrderItem orderItem;

    @Field("input_quantity")
    private Integer inputQuantity;

    @Field("output_quantity")
    private Integer outputQuantity;

    @Field("input_return_quantity")
    private Integer inputReturnQuantity;

    @Field("return_order")
    @Builder.Default
    private boolean returnOrder = false;
}
