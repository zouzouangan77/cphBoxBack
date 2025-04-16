package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@org.springframework.data.mongodb.core.mapping.Document(collection = "log")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ApplicationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @DocumentReference
    private User user;

    @NotNull
    private String operation;

    @NotNull
    @Field("end_point")
    private String endPoint;

    @NotNull
    private String method;

    private String params;

    @CreatedDate
    @Builder.Default
    @Field("created_date")
    private Instant createdDate = Instant.now();
}
