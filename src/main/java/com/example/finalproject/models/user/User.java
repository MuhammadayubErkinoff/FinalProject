package com.example.chorvoqgisbackend.models.user;

import com.example.chorvoqgisbackend.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FilterDef(name = "userFilter",
        parameters = {@ParamDef(name = "name", type = String.class),
                @ParamDef(name = "roleId", type = Long.class),
                @ParamDef(name = "departmentId", type = Long.class)})
@Filter(name = "userFilter",
        condition = "(:name IS NULL OR (first_name ILIKE '%' || :name || '%') OR (last_name ILIKE '%' || :name || '%') OR (login ILIKE '%' || :name || '%')) AND" +
                "(:roleId IS NULL OR role_id = :roleId) AND" +
                "(:departmentId IS NULL OR department_id = :departmentId)")
public class User extends BaseModel implements UserDetails {

    private String firstName;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String login;
    @Column(nullable = false)
    private String password;
    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;
    private Long departmentId;

    @Override
    @Transient @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    @Transient @JsonIgnore
    public String getUsername() {
        return login;
    }

    @Override
    @Transient @JsonIgnore
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    @Transient @JsonIgnore
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    @Transient @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    @Transient @JsonIgnore
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public void hide(){
        super.hide();
        if(role!=null) role.hide();
        setPassword(null);
    }
}
