package com.psu.rouen.cphbox.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.psu.rouen.cphbox.config.Constants;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A user.
 */
@org.springframework.data.mongodb.core.mapping.Document(collection = "user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class User extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Indexed
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    private String password;

    @Size(max = 50)
    @Field("first_name")
    private String firstName;

    @Size(max = 50)
    @Field("last_name")
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Indexed
    private String email;

    @Builder.Default
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Field("lang_key")
    private String langKey;

    @Size(max = 256)
    @Field("image_url")
    private String imageUrl;

    @Size(max = 20)
    @Field("activation_key")
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Field("reset_key")
    @JsonIgnore
    private String resetKey;

    @Field("reset_date")
    @Builder.Default
    private Instant resetDate = null;

    @JsonIgnore
    @Builder.Default
    private Set<Authority> authorities = new HashSet<>();
}
