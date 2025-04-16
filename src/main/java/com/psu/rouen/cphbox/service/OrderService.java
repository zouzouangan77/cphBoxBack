package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.domain.*;
import com.psu.rouen.cphbox.repository.*;
import com.psu.rouen.cphbox.security.OrderConstants;
import com.psu.rouen.cphbox.service.dto.OrderDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final OrderItemRepository orderItemRepository;
    private final BoxRepository boxRepository;
    private final BoxCatalogRepository boxCatalogRepository;
    private final BoxStockRepository boxStockRepository ;





    private final MongoTemplate mongoTemplate;

    public OrderService(

        EventRepository eventRepository, OrderRepository orderRepository,

        UserRepository userRepository, OrderItemRepository orderItemRepository,
        BoxRepository boxRepository, BoxCatalogRepository boxCatalogRepository,
        BoxStockRepository boxStockRepository, MongoTemplate mongoTemplate
    ) {
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.boxRepository = boxRepository;
        this.boxCatalogRepository = boxCatalogRepository;
        this.boxStockRepository = boxStockRepository;

        this.mongoTemplate = mongoTemplate;
    }

    //    @Transactional(rollbackFor = BadRequestAlertException.class)
    public OrderDTO createOrder(OrderDTO orderDTO) {
        if (StringUtils.isBlank(orderDTO.getReference())) {
            orderDTO.setReference(ServiceUtils.generateReference());
        }
        Order order  = createUpdateProcessOrder(orderDTO, false, false);
        return new OrderDTO(order);
    }

    //   @Transactional(rollbackFor = BadRequestAlertException.class)
    public Optional<OrderDTO> updateOrder(OrderDTO orderDTO) {

        return Optional.of(createUpdateProcessOrder(orderDTO, true, false)).map(OrderDTO::new);
    }

    public Optional<OrderDTO> processOrder(OrderDTO orderDTO) {

        return Optional.of(createUpdateProcessOrder(orderDTO, true, true)).map(OrderDTO::new);
    }



    private void validateOrderDependencies(Order order) {
        String userId = order.getUser().getId();
        String eventId = order.getEvent().getId();

        userRepository.findById(userId).orElseThrow(
            () -> new BadRequestAlertException("User not found", "Order Saving", "UserNotFound")
        );

        eventRepository.findById(eventId).orElseThrow(
            () -> new BadRequestAlertException("Event not found", "Order Saving", "EventNotFound")
        );
    }

    private void validateAndSetBoxCatalog(OrderItem orderItem) {
        BoxCatalog boxCatalog = orderItem.getBoxCatalog();
        if (boxCatalog == null || boxCatalog.getId() == null) {
            throw new BadRequestAlertException("Empty or invalid BoxCatalog", "Order Saving", "InvalidBoxCatalog");
        }
        boxCatalogRepository.findById(boxCatalog.getId()).orElseThrow(
            () -> new BadRequestAlertException("BoxCatalog not found", "Order Saving", "NotFoundBoxCatalog")
        );
    }

    private void handleItemInProcess(OrderItem orderItem) {
        if(orderItem.getIsSelected()){
            if (orderItem.getOrderItemRetrieved()){
                orderItem.setStatus(OrderConstants.ORDERITEM_RECOVER);
            }else{
                    orderItem.setOrderItemRetrieved(true);
                    orderItem.setStatus(OrderConstants.ORDERITEM_RECOVER);
                    if (orderItem.getComment().equals(OrderConstants.ORDERITEM_NOT_FOUND)){
                        orderItem.getBoxCatalog().getBoxStocks().forEach(boxStock -> {
                            boxStock.setOutputQuantity(orderItem.getOrderItemQuantityInitialWanted());
                            boxStockRepository.save(boxStock);
                        });
                    }
            }
        }else{
            if (orderItem.getOrderItemRetrieved()){
                orderItem.setStatus(OrderConstants.ORDERITEM_RECOVER);
                orderItem.setIsSelected(true);
            }else {
                orderItem.setStatus(OrderConstants.ORDERITEM_NO_RECOVER);
            }
        }

    }






    private void manageExistingOrder(Order order) {
        Optional<Order> oldOrder = orderRepository.findById(order.getId());

        if (oldOrder.isEmpty()) {
            throw new BadRequestAlertException("Order not found", "Order Updating", "NotFoundOrder");
        }

        Set<OrderItem> oldOrderItems = oldOrder.get().getOrderItems();
        if (!CollectionUtils.isEmpty(oldOrderItems)) {
            oldOrderItems.forEach(orderItem -> {
                // Suppression des BoxStocks associés à chaque OrderItem
                if (!CollectionUtils.isEmpty(orderItem.getBoxCatalog().getBoxStocks())) {
                    boxStockRepository.deleteAll(orderItem.getBoxCatalog().getBoxStocks());
                }

            });

            // Suppression des OrderItems
            orderItemRepository.deleteAll(oldOrderItems);
        }
    }

    public void deleteOrder(String id) {
        orderRepository
            .findById(id)
            .ifPresent(order -> {
                if (!CollectionUtils.isEmpty(order.getOrderItems())) {
                    order
                        .getOrderItems()
                        .forEach(orderItems -> {
                            if (!CollectionUtils.isEmpty(orderItems.getBoxCatalog().getBoxStocks())) {
                                orderItems
                                    .getBoxCatalog()
                                    .getBoxStocks()
                                    .forEach(bc->{
                                        bc.setOutputQuantity(0);
                                        boxStockRepository.save(bc);
                                    });

                            }
                            orderItemRepository.delete(orderItems);
                        });
                }
                orderRepository.delete(order);
                log.debug("Deleted Order: {}", order);
            });
    }




    private Set<BoxStock> processBoxStocks(OrderItem orderItem) {
        Set<BoxStock> newBoxStocks = new HashSet<>();
        for (BoxStock boxStock : orderItem.getBoxCatalog().getBoxStocks()) {
            //boxStock.setId(null);  // reset ID for new records
            boxStock.setOutputQuantity(orderItem.getQuantity());
            boxStock.setBoxCatalog(orderItem.getBoxCatalog());

            newBoxStocks.add(boxStockRepository.save(boxStock));

        }
        return newBoxStocks;
    }

    private Set<OrderItem> processOrderItems(Order order, boolean isProcess, boolean isUpdate) {
        Set<OrderItem> newOrderItems = new HashSet<>();

        for (OrderItem orderItem : order.getOrderItems()) {
            validateAndSetBoxCatalog(orderItem);
            Set<BoxStock> newBoxStocks = processBoxStocks(orderItem);
            orderItem.getBoxCatalog().setBoxStocks(newBoxStocks);

            // Cas d'un nouvel ajout (isUpdate == false)
            if (!isUpdate) {
                orderItem.setId(null);
                orderItem.setOrderItemQuantityInitialWanted(orderItem.getQuantity());
                orderItem.setOrderItemRetrieved(false);
                orderItem.setIsSelected(false);
            } else {
                // Cas d'une modification (isUpdate == true)
                if (isProcess) {
                    handleItemInProcess(orderItem);
                } else {
                    orderItem.setOrderItemQuantityInitialWanted(orderItem.getQuantity());
                    orderItem.setOrderItemRetrieved(false);
                    orderItem.setIsSelected(false);
                }

            }

            orderItem.setOrder(order);
            newOrderItems.add(orderItemRepository.save(orderItem));
        }

        return newOrderItems;
    }


    private Order createUpdateProcessOrder(OrderDTO orderDTO, boolean isUpdate, boolean isProcess) {

        Order order = orderDTO.dtoToEntity();
        validateOrderDependencies(order);

        // Gestion de la quantité initiale de order souhaitée
        if (!isUpdate && !isProcess) {
            order.setOrderQuantityInitialWanted(order.getQuantity());
            order.setStatus(OrderConstants.ORDER_NEW);
        } else if (isUpdate && !isProcess) {
            order.setOrderQuantityInitialWanted(order.getQuantity());
            order.setStatus(OrderConstants.ORDER_NEW);
        }

        order = orderRepository.save(order);

        if (isUpdate) {
            manageExistingOrder(order);
        }


        Set<OrderItem> orderItems = processOrderItems(order, isProcess,isUpdate);
        order.setOrderItems(orderItems);
        if(isProcess){
            boolean allItemsRetrieved = order.getOrderItems().stream()
                .allMatch(OrderItem::getOrderItemRetrieved);

            if (allItemsRetrieved) {
                order.setStatus(OrderConstants.ORDER_DONE);
            }else {
                order.setStatus(OrderConstants.ORDER_IN_PREPARATION);
            }

        }

        return orderRepository.save(order);
    }




    public Page<OrderDTO> getAllOrder(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderDTO::new);
    }

    public Page<OrderDTO>searchAllOrder(String search, Pageable pageable) {
        LookupOperation lookupOperationEVT = LookupOperation
            .newLookup()
            .from("event")
            .localField("event")
            .foreignField("_id")
            .as("evt");

        LookupOperation lookupOperationUSR = LookupOperation
            .newLookup()
            .from("user")
            .localField("user")
            .foreignField("_id")
            .as("usr");

        LookupOperation lookupOperationOI = LookupOperation
            .newLookup()
            .from("order_item")
            .localField("order_items")
            .foreignField("_id")
            .as("orderitems");

        LookupOperation lookupOperationBC = LookupOperation
            .newLookup()
            .from("box_catalog")
            .localField("orderitems.box_catalog")
            .foreignField("_id")
            .as("boxcatalog");

        LookupOperation lookupOperationCat = LookupOperation
            .newLookup()
            .from("catalog")
            .localField("boxcatalog.catalog")
            .foreignField("_id")
            .as("catalog");

        MatchOperation matchOperation = Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("reference").regex(search, "i"),
                    Criteria.where("status").regex(search, "i"),
                    Criteria.where("usr.login").regex(search, "i"),
                    Criteria.where("evt.name").regex(search, "i"),
                    Criteria.where("catalog.book").regex(search, "i")
                )
        );

        Aggregation aggregationCount = Aggregation.newAggregation(
            lookupOperationOI,
            lookupOperationUSR,
            lookupOperationEVT,
            lookupOperationBC,
            lookupOperationCat,
            matchOperation,
            Aggregation.count().as("count")
        );

        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "order", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        Aggregation aggregation = Aggregation.newAggregation(
            lookupOperationOI,
            lookupOperationUSR,
            lookupOperationEVT,
            lookupOperationBC,
            lookupOperationCat,
            matchOperation,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );

        List<OrderDTO> orderList = mongoTemplate
            .aggregate(aggregation, "order", Order.class)
            .getMappedResults()
            .stream()
            .map(OrderDTO::new)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(orderList, pageable, () -> totalCount);

    }

    public List<MissingOrderItem> calculateMissingItems(Order order) {
        List<MissingOrderItem> missingOrderItems = new ArrayList<>();

        // Parcourir les items de la commande
        for (OrderItem item : order.getOrderItems()) {
            // Vérifier si la quantité récupérée est inférieure à la quantité demandée
            if (item.getQuantity()< item.getOrderItemQuantityInitialWanted()) {
                MissingOrderItem missingOrderItem = new MissingOrderItem();
                missingOrderItem.setBook(item.getBoxCatalog().getCatalog().getBook());
                missingOrderItem.setMissingQuantity(item.getOrderItemQuantityInitialWanted() - item.getQuantity());
                missingOrderItem.setReason(item.getComment()); // Vous pouvez adapter la raison si elle est dynamique
                missingOrderItems.add(missingOrderItem);
            }
        }

        return missingOrderItems;
    }

    public Map<String, Object> getOrderDetails(Order order) {
        Map<String, Object> orderDetails = new HashMap<>();
        int totalRecoveredQuantity = 0;
        double totalPrice = 0.0;

        // Articles manquants
        List<MissingOrderItem> missingOrderItems = calculateMissingItems(order);

        // Parcourir les items de la commande
        for (OrderItem item : order.getOrderItems()) {
            totalRecoveredQuantity += item.getQuantity();
            totalPrice += item.getQuantity() * item.getBoxCatalog().getCatalog().getPrice();
        }

        // Ajouter les détails à la réponse
        orderDetails.put("orderReference", order.getReference());
        orderDetails.put("orderDate", order.getOrderDate());
        orderDetails.put("totalRecoveredQuantity", totalRecoveredQuantity);
        orderDetails.put("totalPrice", totalPrice);
        orderDetails.put("missingItems", missingOrderItems);

        return orderDetails;
    }


}


