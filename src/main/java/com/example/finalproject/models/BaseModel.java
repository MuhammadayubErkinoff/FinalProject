package com.example.chorvoqgisbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Data
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedFilter", condition = "deleted = :isDeleted")
public class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedBy
    private String  createdBy;
    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;
    @LastModifiedBy
    private String  lastUpdatedBy;
    @Column(nullable = false)
    private Boolean deleted=false;

    public void clear(){
        id=null;
        createdAt=null;
        createdBy=null;
        lastUpdatedAt=null;
        lastUpdatedBy=null;
        deleted=false;
    }

    public void hide(){
        createdAt=null;
        createdBy=null;
        lastUpdatedAt=null;
        lastUpdatedBy=null;
        deleted=null;
    }
}
