package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "order")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false,exclude = "orderItems")
public class Order extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Unique
    private String reference;

    @DocumentReference
    private Event event;

    @DocumentReference
    private User user; //order for this user, can't be null if order it's not for a particuliar user

    @Field("order_date")
    private Date orderDate;


    private String comment;

    private String operatorComment;

    private Integer quantity;

    private Integer orderQuantityInitialWanted;

    //@Builder.Default
    @Field("order_items")
    @DocumentReference(collection = "order_item")
    @ToString.Exclude
    private Set<OrderItem> orderItems = new HashSet<>();

    private String status;


}
