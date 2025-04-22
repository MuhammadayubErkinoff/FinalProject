package com.example.chorvoqgisbackend.models.cms;


import com.example.chorvoqgisbackend.models.BaseModel;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FilterDef(name = "instructionFilter", parameters = {@ParamDef(name = "query", type = String.class), @ParamDef(name = "parentId", type = Long.class)})
@Filter(name = "instructionFilter", condition = "(:query IS NULL OR title_uz ILIKE '%' || :query || '%' OR content_uz ILIKE '%' || :query || '%'" +
        "OR title_ru ILIKE '%' || :query || '%' OR content_ru ILIKE '%' || :query || '%'" +
        "OR title_eng ILIKE '%' || :query || '%' OR content_eng ILIKE '%' || :query || '%')" +
        "AND (:parentId IS NULL OR parent_id = :parentId)")

public class Instruction extends BaseModel {

    String slug;
    String titleUz;
    String titleRu;
    String titleEng;
    String contentUz;
    String contentRu;
    String contentEng;
    Long parentId;

}
