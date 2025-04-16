package com.psu.rouen.cphbox.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An authority (a security role) used by Spring Security.
 */
@Document(collection = "authority")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Getter
public class Authority implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Size(max = 50)
    @Id
    private String name;
}
