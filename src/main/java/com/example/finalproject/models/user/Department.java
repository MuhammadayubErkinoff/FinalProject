package com.example.finalproject.models.user;

import com.example.finalproject.models.BaseModel;
import jakarta.persistence.Column;
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
@FilterDef(name = "departmentFilter", parameters = {@ParamDef(name = "name", type = String.class)})
@Filter(name = "departmentFilter", condition = "(:name IS NULL OR name ILIKE '%' || :name || '%' )")
public class Department extends BaseModel {

    @Column(unique = true, nullable = false)
    String name;

    @Override
    public void hide(){
        setLastUpdatedAt(null);
        setLastUpdatedBy(null);
        setDeleted(null);
    }
}
