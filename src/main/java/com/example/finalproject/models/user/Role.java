package com.example.finalproject.models.user;

import com.example.finalproject.models.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FilterDef(name = "roleFilter", parameters = {@ParamDef(name = "name", type = String.class)})
@Filter(name = "roleFilter", condition = "(:name IS NULL OR name ILIKE '%' || :name || '%')")
public class Role extends BaseModel {

    @Column(nullable = false, unique = true)
    private String name;
    @ManyToMany(fetch=FetchType.EAGER)
    private Set<Action> actions;


    @Override
    public void hide(){
        super.hide();
        if(actions!=null)actions=actions.stream().peek(Action::hide).collect(Collectors.toSet());
    }

}
