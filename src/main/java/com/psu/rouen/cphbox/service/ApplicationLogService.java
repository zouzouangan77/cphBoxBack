package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.domain.ApplicationLog;
import com.psu.rouen.cphbox.repository.ApplicationLogRepository;
import com.psu.rouen.cphbox.web.rest.vm.ApplicationLogVM;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicationLogService {

    private final ApplicationLogRepository applicationLogRepository;
    private final MongoTemplate mongoTemplate;

    public ApplicationLogService(ApplicationLogRepository applicationLogRepository, MongoTemplate mongoTemplate) {
        this.applicationLogRepository = applicationLogRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void save(ApplicationLog applicationLog) {
        applicationLogRepository.save(applicationLog);
    }

    public Page<ApplicationLogVM> searchAllBox(String search, Pageable pageable) {
        LookupOperation lookupOperationWH = LookupOperation
            .newLookup()
            .from("user")
            .localField("user")
            .foreignField("_id")
            .as("user");

        MatchOperation matchOperation = Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("operation").regex(search, "i"),
                    Criteria.where("end_point").regex(search, "i"),
                    Criteria.where("method").regex(search, "i"),
                    Criteria.where("params").regex(search, "i"),
                    Criteria.where("create_date").regex(search, "i"),
                    Criteria.where("user.login").regex(search, "i")
                )
        );

        Aggregation aggregationCount = Aggregation.newAggregation(
            lookupOperationWH,
            matchOperation,
            Aggregation.count().as("count")
        );
        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "log", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        Aggregation aggregation = Aggregation.newAggregation(
            lookupOperationWH,
            matchOperation,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );

           List<ApplicationLogVM> logList = mongoTemplate
            .aggregate(aggregation, "log", ApplicationLog.class)
            .getMappedResults()
            .stream()
            .map(ApplicationLogVM::new)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(logList, pageable, () -> totalCount);
    }
}
