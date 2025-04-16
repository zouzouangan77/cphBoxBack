package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.config.ApplicationProperties;
import com.psu.rouen.cphbox.domain.MissingOrderItem;
import com.psu.rouen.cphbox.domain.Order;
import com.psu.rouen.cphbox.domain.User;
import com.psu.rouen.cphbox.repository.OrderItemRepository;
import com.psu.rouen.cphbox.repository.OrderRepository;
import com.psu.rouen.cphbox.repository.UserRepository;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;
import com.psu.rouen.cphbox.service.MailService;
import com.psu.rouen.cphbox.service.OrderService;
import com.psu.rouen.cphbox.service.dto.OrderDTO;
import com.psu.rouen.cphbox.service.dto.UserDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

@RestController
@RequestMapping("/api")
@Slf4j
public class OrderResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "reference", "event", "user","status", "createdBy","orderItems", "createdDate", "lastModifiedBy", "lastModifiedDate")
    );
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    private final SpringTemplateEngine templateEngine;

    private final ApplicationProperties applicationProperties;

    private final MailService mailService;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public OrderResource(
        OrderService orderService,
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository, UserRepository userRepository, SpringTemplateEngine templateEngine,
        ApplicationProperties applicationProperties,
        MailService mailService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.templateEngine = templateEngine;
        this.applicationProperties = applicationProperties;
        this.mailService = mailService;
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.ORDER_CRUD + "\")")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) throws URISyntaxException {

        if (StringUtils.isNoneBlank(orderDTO.getId())) {
            throw new BadRequestAlertException("A new order cannot already have an ID", "orderManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else {
            log.debug("OrderDTO reçu avec orderItems : {}", orderDTO.getOrderItems());

            OrderDTO createOrder = orderService.createOrder(orderDTO);
            Optional<User> user= userRepository.findById(createOrder.getId());
            if (user.isPresent()){

            }

            mailService.sendConfirmationOrderMail(createOrder.dtoToEntity());

            mailService.sendLivraisonEnCoursOrderMail(createOrder.dtoToEntity());
            mailService.sendLivrerOrderMail(createOrder.dtoToEntity());


            return ResponseEntity
                .created(new URI("/api/orders/" + createOrder.getId()))
                .headers(
                    HeaderUtil.createAlert(applicationName, "A order is created with identifier " + createOrder.getId(), createOrder.getId())
                )
                .body(createOrder);
        }
    }

    @PutMapping("/orders")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.ORDER_CRUD + "\")")
    public ResponseEntity<OrderDTO> updateOrder(@Valid @RequestBody OrderDTO orderDTO) {
        log.debug("REST request to update Order : {}", orderDTO);
        if (StringUtils.isBlank(orderDTO.getId()) || StringUtils.isBlank(orderDTO.getReference())) {
            throw new BadRequestAlertException("A update order must have a ID and REFERENCE", "orderManagement", "idEmptyOrReferenceEmpty");
        }
        Optional<Order> existingOrder = orderRepository.findOneByReference(orderDTO.getReference());
        if (existingOrder.isPresent() && (!existingOrder.get().getId().equals(orderDTO.getId()))) {
            throw new BadRequestAlertException("A update has already use, please change reference", "orderManagement", "referenceAlreadyUse");
        }

        Optional<OrderDTO> updateOrder = orderService.updateOrder(orderDTO);
        updateOrder.ifPresent(dto -> mailService.sendConfirmationModificationOrderMail(dto.dtoToEntity()));

        return ResponseUtil.wrapOrNotFound(
            updateOrder,
            HeaderUtil.createAlert(applicationName, "A order is updated with identifier " + orderDTO.getId(), orderDTO.getId())
        );
    }

    @PutMapping("/orders/process")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.ORDER_CRUD + "\")")
    public ResponseEntity<OrderDTO> ProcessOrder(@Valid @RequestBody OrderDTO orderDTO) {
        log.debug("REST request to process Order : {}", orderDTO);
        if (StringUtils.isBlank(orderDTO.getId()) || StringUtils.isBlank(orderDTO.getReference())) {
            throw new BadRequestAlertException("A process order must have a ID and REFERENCE", "orderManagement", "idEmptyOrReferenceEmpty");
        }
        Optional<Order> existingOrder = orderRepository.findOneByReference(orderDTO.getReference());
        if (existingOrder.isPresent() && (!existingOrder.get().getId().equals(orderDTO.getId()))) {
            throw new BadRequestAlertException("A process has already use, please change reference", "orderManagement", "referenceAlreadyUse");
        }

        Optional<OrderDTO> processOrder = orderService.processOrder(orderDTO);
        processOrder.ifPresent(dto -> mailService.sendPriseEnChargeOrderMail(dto.dtoToEntity()));
        return ResponseUtil.wrapOrNotFound(
            processOrder,
            HeaderUtil.createAlert(applicationName, "A order is processed with identifier " + orderDTO.getId(), orderDTO.getId())
        );
    }

    @DeleteMapping("/orders/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.ORDER_CRUD + "\")")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        log.debug("REST request to order Order: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createAlert(applicationName, "A order is deleted with identifier " + id, id))
            .build();
    }
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all orders for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<OrderDTO> page = orderService.getAllOrder(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable String id) {
        log.debug("REST request to get Order By id : {}", id);
        return ResponseUtil.wrapOrNotFound(orderRepository.findById(id).map(OrderDTO::new));
    }

    @GetMapping("/orders/reference/{reference}")
    public ResponseEntity<OrderDTO> getOrderByReference(@PathVariable String reference) {
        log.debug("REST request to get Order By reference : {}", reference);
        Optional<Order> order = orderRepository.findOneByReference(reference);
        if (order.isPresent()) {
            log.debug("Order found with reference : {}", reference);
            return ResponseEntity.ok(new OrderDTO(order.get()));
        } else {
            log.debug("No order found with reference : {}", reference);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/orders/user/{iduser}")
    public List<OrderDTO> getOrdersByUser(@PathVariable String iduser) {
        log.debug("REST request to get all users");
        return orderRepository
            .findAllByUser(User.builder().id(iduser).build())
            .stream().map(OrderDTO::new)
            .collect(Collectors.toList());

    }


    @GetMapping("/orders/search/{search}")
    public ResponseEntity<List<OrderDTO>> getAllOrderssbySearch(
        @PathVariable String search,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<OrderDTO> page = orderService.searchAllOrder(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    @GetMapping(value = "/orders/pdf/bonCommand/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> getPdfBonOrder(@PathVariable String id) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Optional<OrderDTO> opOrder = orderRepository.findById(id).map(OrderDTO::new);
        log.debug("eleùmet++++++++++++++++++ : {}",opOrder);

        if (opOrder.isPresent()) {
            //Locale locale = new Locale();
            Context context = new Context();
            OrderDTO order = opOrder.get();

            log.debug("eleùmet************************* : {}",order);
            Optional<UserDTO> opUser = userRepository.findById(order.getUser().getId()).map(UserDTO::new);


            if(opUser.isPresent()){
                UserDTO user = opUser.get();
                String clientLastName = user.getLastName();
                String clientFirstName = user.getFirstName();
                String clientEmail =user.getEmail();

                context.setVariable("clientLastName", clientLastName);
                context.setVariable("clientFirstName", clientFirstName);
                context.setVariable("clientEmail", clientEmail);
            }

            double priceTotal = order.getOrderItems()
                .stream()
                .mapToDouble(orderItemDTO -> orderItemDTO.getOrderItemQuantityInitialWanted() * orderItemDTO.getBoxCatalog().getCatalog().getPrice())
                .sum();

            context.setVariable("priceTotal",priceTotal);

            Instant createdDate = order.getCreatedDate();


            LocalDateTime dateTime = createdDate.atZone(ZoneId.of("UTC")).toLocalDateTime();

            // Formatter la date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String orderDate = dateTime.format(formatter);


            context.setVariable("order", order);
            context.setVariable("orderDate",orderDate);


            String content = templateEngine.process("order/orderTagInfoPdf", context);

            // Create an ITextRenderer instance
            ITextRenderer renderer = new ITextRenderer();

            // Generate the PDF from an XHTML file or HTML content
            renderer.setDocumentFromString(content);

            renderer.layout();
            renderer.createPDF(outputStream);
            // Create an InputStreamResource from the output stream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            // Set the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=order-" + order.getReference() + "_" + Instant.now().getEpochSecond() + ".pdf"
            );
            headers.add(HttpHeaders.CONTENT_TYPE, "multipart / form-data");

            // Return the PDF as a ResponseEntity with the appropriate headers
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(inputStream));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/orders/pdf/summaryCommand/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> getPdfSummaryOrder(@PathVariable String id){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Optional<OrderDTO> opOrder = orderRepository.findById(id).map(OrderDTO::new);


        if (opOrder.isPresent()) {
            //Locale locale = new Locale();
            Context context = new Context();
            OrderDTO order = opOrder.get();
            Optional<UserDTO> opUser = userRepository.findById(order.getUser().getId()).map(UserDTO::new);

            Map<String, Object> orderDetails = orderService.getOrderDetails(order.dtoToEntity());
            List<MissingOrderItem> missingOrderItems = orderService.calculateMissingItems(order.dtoToEntity());
            context.setVariable("orderDetails", orderDetails);


            double totalPrice = order.getOrderItems()
                .stream()
                .mapToDouble(orderItemDTO -> orderItemDTO.getQuantity() * orderItemDTO.getBoxCatalog().getCatalog().getPrice())
                .sum();

            context.setVariable("totalPrice",totalPrice);
            context.setVariable("missingOrderItems",missingOrderItems);

            Instant createdDate = order.getCreatedDate();


            LocalDateTime dateTime = createdDate.atZone(ZoneId.of("UTC")).toLocalDateTime();

            // Formatter la date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String orderDate = dateTime.format(formatter);


            context.setVariable("order", order);
            context.setVariable("orderDate",orderDate);


            String content = templateEngine.process("order/orderTagSummaryPdf", context);

            // Create an ITextRenderer instance
            ITextRenderer renderer = new ITextRenderer();

            // Generate the PDF from an XHTML file or HTML content
            renderer.setDocumentFromString(content);

            renderer.layout();
            renderer.createPDF(outputStream);
            // Create an InputStreamResource from the output stream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            // Set the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=order-" + order.getReference() + "_" + Instant.now().getEpochSecond() + ".pdf"
            );
            headers.add(HttpHeaders.CONTENT_TYPE, "multipart / form-data");

            // Return the PDF as a ResponseEntity with the appropriate headers
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(inputStream));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    }

}
