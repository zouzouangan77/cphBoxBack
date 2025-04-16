package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.domain.Event;
import com.psu.rouen.cphbox.domain.Warehouse;
import com.psu.rouen.cphbox.repository.*;
import com.psu.rouen.cphbox.service.dto.BoxDTO;
import com.psu.rouen.cphbox.service.dto.EventDTO;
import com.psu.rouen.cphbox.service.dto.WarehouseDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;

    private final MongoTemplate mongoTemplate;

    public EventService(
        EventRepository eventRepository,
        OrderRepository orderRepository,
        MongoTemplate mongoTemplate
    ) {
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
    }
    public EventDTO createEvent(EventDTO eventDTO) {
        Event event = createUpdateEvent(eventDTO, false);
        return new EventDTO(event);
    }

    public Optional<EventDTO> updateEvent(EventDTO eventDTO) {
        createUpdateEvent(eventDTO, true);
        return Optional.of(createUpdateEvent(eventDTO, true)).map(EventDTO::new);
    }
    public Page<EventDTO> getAllEvent(Pageable pageable) {
        return eventRepository.findAll(pageable).map(EventDTO::new);
    }
    private Event createUpdateEvent(EventDTO eventDTO, Boolean isUpdate) {
        Event event = eventDTO.dtoToEntity();
        final String id = event.getId();
        if (isUpdate) {
            if (StringUtils.isBlank(id) || eventRepository.findById(id).isEmpty()) {
                throw new BadRequestAlertException(
                    "Event '" + event.getId() + "' not found",
                    "Event Updating",
                    "NotFound Event "
                );
            }

            eventRepository
                .findOneByNameIgnoreCase(event.getName())
                .ifPresent(event1 -> {
                    if (!event1.getId().equals(id)) {
                        throw new BadRequestAlertException(
                            "Event '" + event1.getName() + "' Already Exists",
                            "Event Saving",
                            "Already Exists"
                        );
                    }
                });
        } else {
            if (eventRepository.findOneByNameIgnoreCase(event.getName()).isPresent()) {
                throw new BadRequestAlertException(
                    "Event '" + event.getName() + "' Already Exists",
                    "Event Saving",
                    "Already Exists"
                );
            }
        }

        if (StringUtils.isBlank(event.getId())) {
            event.setId(null);
        }

        event = eventRepository.save(event);

        return event;
    }
    public void deleteEvent(String id) {
        eventRepository
            .findById(id)
            .ifPresent(event -> {
                orderRepository
                    .findFirstByEvent(event)
                    .ifPresentOrElse(
                        order -> {
                            throw new BadRequestAlertException(
                                "Impossible to delete Event is already used by a order",
                                "Event deleting",
                                "Already used somewhere"
                            );

                        },
                        ()->{
                            eventRepository.delete(event);
                        }
                    );
            });
    }



    public Page<EventDTO> searchAllEvent(String search, Pageable pageable){
        MatchOperation matchOperation = Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("name").regex(search, "i"),
                    Criteria.where("address").regex(search, "i"),
                    Criteria.where("date").regex(search, "i"),
                    Criteria.where("comment").regex(search, "i")

                )
        );
        Aggregation aggregationCount = Aggregation.newAggregation(
            matchOperation,
            Aggregation.count().as("count")
        );
        Aggregation aggregation = Aggregation.newAggregation(
            matchOperation,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );
        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "event", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        List<EventDTO> eventList = mongoTemplate
            .aggregate(aggregation, "event", Event.class)
            .getMappedResults()
            .stream()
            .map(EventDTO::new)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(eventList, pageable, () -> totalCount);
    }

}
