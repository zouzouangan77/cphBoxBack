package com.psu.rouen.cphbox.service.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.psu.rouen.cphbox.domain.*;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @ToString.Exclude
    private OrderDTO order;

    private Integer quantity;

    private BoxCatalogDTO boxCatalog;

    private BoxDTO box;

    private String status;

    private Boolean orderItemRetrieved;

    private Integer orderItemQuantityInitialWanted;

    private Boolean isSelected;

    private String comment;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    public static OrderItemDTO createSimple(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        return OrderItemDTO
            .builder()
            .id(orderItem.getId())
            .order(OrderDTO.createSimple(orderItem.getOrder()))
            .quantity(orderItem.getQuantity())
            .boxCatalog(new BoxCatalogDTO(orderItem.getBoxCatalog()))
            .box(new BoxDTO(orderItem.getBox()))
            .status(orderItem.getStatus())
            .comment(orderItem.getComment())
            .orderItemRetrieved(orderItem.getOrderItemRetrieved())
            .isSelected(orderItem.getIsSelected())
            .orderItemQuantityInitialWanted(orderItem.getOrderItemQuantityInitialWanted())
            .createdDate(orderItem.getCreatedDate())
            .createdBy(orderItem.getCreatedBy())
            .lastModifiedDate(orderItem.getLastModifiedDate())
            .lastModifiedBy(orderItem.getLastModifiedBy())
            .build();
    }


    public OrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return;
        }

        this.id = orderItem.getId();
        this.order = orderItem.getOrder() != null ? OrderDTO.createSimple(orderItem.getOrder()) : null;
        this.quantity = orderItem.getQuantity();
        this.boxCatalog = orderItem.getBoxCatalog()!= null ? BoxCatalogDTO.createSimple(orderItem.getBoxCatalog()): null;
        this.box = orderItem.getBox() !=null ? BoxDTO.createSimple(orderItem.getBox()) : null;
        this.status = orderItem.getStatus();
        this.orderItemRetrieved=orderItem.getOrderItemRetrieved();
        this.isSelected=orderItem.getIsSelected();
        this.orderItemQuantityInitialWanted=orderItem.getOrderItemQuantityInitialWanted();
        this.comment=orderItem.getComment();
        this.createdDate = orderItem.getCreatedDate();
        this.createdBy = orderItem.getCreatedBy();
        this.lastModifiedDate = orderItem.getLastModifiedDate();
        this.lastModifiedBy = orderItem.getLastModifiedBy();
    }




    public OrderItem dtoToEntity() {
        return OrderItem
            .builder()
            .id(this.id)
            .order(this.order != null ? this.order.dtoToEntity() : null)
            .quantity(this.quantity)
            .boxCatalog(this.boxCatalog != null ? this.boxCatalog.dtoToEntity() : null)
            .box(this.box != null ? this.box.dtoToEntity() : null)
            .status(this.status)
            .orderItemRetrieved(this.orderItemRetrieved)
            .isSelected(this.isSelected)
            .orderItemQuantityInitialWanted(this.orderItemQuantityInitialWanted)
            .comment(this.comment)
            .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity, status,orderItemRetrieved, createdDate, createdBy, lastModifiedBy, lastModifiedDate);
    }

}
