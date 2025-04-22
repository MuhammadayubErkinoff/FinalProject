package com.example.chorvoqgisbackend.models.user;

import com.example.chorvoqgisbackend.models.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FilterDef(name = "actionFilter",
        parameters = {@ParamDef(name = "name", type = String.class),
                @ParamDef(name = "allowedPath", type = String.class)})
@Filter(name = "actionFilter",
        condition = "(:name IS NULL OR name ILIKE '%' || :name || '%' OR description ILIKE '%' || :name || '%') AND" +
                "(:allowedPath IS NULL OR allowed_path ILIKE '%' || :allowedPath || '%')")
public class Action extends BaseModel {
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
    @Column(nullable = false)
    private String allowedPath;
    @Column(nullable = false, columnDefinition = "varchar[]")
    private Set<String> allowedMethods;
}
