package com.psu.rouen.cphbox.service.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.psu.rouen.cphbox.domain.Box;
import com.psu.rouen.cphbox.domain.Order;

import lombok.*;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with only the public attributes.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String reference;
    @NotNull
    private EventDTO event;
    @NotNull
    private UserDTO user;

    private String comment;

    private String operatorComment;

    private Date orderDate;

    private Integer quantity;

    private Integer orderQuantityInitialWanted;

    private String status;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;


    @ToString.Exclude
    private Set<OrderItemDTO> orderItems;

    public static OrderDTO createSimple(Order order) {
        if (order == null) {
            return null;
        }

        return OrderDTO
            .builder()
            .id(order.getId())
            .reference(order.getReference())
            .event(order.getEvent() != null ? EventDTO.createSimple(order.getEvent()) : null)
            .user(order.getUser() != null ? new UserDTO(order.getUser()) : null)
            .comment(order.getComment())
            .operatorComment(order.getOperatorComment())
            .orderDate(order.getOrderDate())
            .quantity(order.getQuantity())
            .orderQuantityInitialWanted(order.getOrderQuantityInitialWanted())
            .status(order.getStatus())
            .createdDate(order.getCreatedDate())
            .createdBy(order.getCreatedBy())
            .lastModifiedDate(order.getLastModifiedDate())
            .lastModifiedBy(order.getLastModifiedBy())
            .build();
    }
    public OrderDTO(Order order) {
        if (order == null) {
            return;
        }
        this.id = order.getId();
        this.reference = order.getReference();
        this.event = order.getEvent() != null ? new EventDTO(order.getEvent()) : null;
        this.user = order.getUser() != null ? new UserDTO(order.getUser()) : null;
        this.comment= order.getComment();
        this.operatorComment=order.getOperatorComment();
        this.orderDate=order.getOrderDate();
        this.quantity= order.getQuantity();
        this.orderQuantityInitialWanted=order.getOrderQuantityInitialWanted();
        this.status = order.getStatus();
        this.orderItems = CollectionUtils.isEmpty(order.getOrderItems())
            ? new HashSet<>()
            : order.getOrderItems().stream().map(OrderItemDTO::createSimple).collect(Collectors.toSet());
        this.createdDate = order.getCreatedDate();
        this.createdBy = order.getCreatedBy();
        this.lastModifiedDate = order.getLastModifiedDate();
        this.lastModifiedBy = order.getLastModifiedBy();
    }




    public Order dtoToEntity() {
        return Order
            .builder()
            .id(this.id)
            .reference(this.reference)
            .event(this.event !=null ? this.event.dtoToEntity() : null)
            .user(this.user !=null ? this.user.dtoToEntity() : null)
            .comment(this.comment)
            .operatorComment(this.operatorComment)
            .orderDate(this.orderDate)
            .status(this.status)
            .quantity(this.quantity)
            .orderQuantityInitialWanted(this.orderQuantityInitialWanted)
            .orderItems(
                CollectionUtils.isEmpty(this.orderItems)
                    ? new HashSet<>()
                    : this.orderItems.stream().map(OrderItemDTO::dtoToEntity).collect(Collectors.toSet())
            )
            .build();
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, reference, status, createdDate, createdBy, lastModifiedBy, lastModifiedDate);
    }
}
