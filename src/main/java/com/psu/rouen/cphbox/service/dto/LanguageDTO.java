package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.Language;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LanguageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    public LanguageDTO(Language language) {
        if (language == null) {
            return;
        }
        this.id = language.getId();
        this.name = language.getName();
    }

    public Language dtoToEntity() {
        return Language.builder().id(this.id).name(this.name).build();
    }
}
