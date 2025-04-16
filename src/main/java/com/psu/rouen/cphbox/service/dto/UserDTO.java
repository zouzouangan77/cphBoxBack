package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.User;
import lombok.Data;

import java.io.Serializable;

/**
 * A DTO representing a user, with only the public attributes.
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String login;
    private String firstName;
    private String lastName;
    private String email;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.lastName = user.getLastName();
        this.firstName = user.getFirstName();
        this.email = user.getEmail();
        this.login = user.getLogin();
    }

    public User dtoToEntity() {
        return User
            .builder()
            .id(this.id)
            .login(this.login)
            .build();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserDTO{" +
            "id='" + id + '\'' +
            ", login='" + login + '\'' +
            "}";
    }
}
