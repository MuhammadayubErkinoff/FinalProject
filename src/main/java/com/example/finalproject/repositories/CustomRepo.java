package com.example.finalproject.repositories;

import com.example.finalproject.models.BaseModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@NoRepositoryBean
public interface CustomRepo<T extends BaseModel, R> extends JpaRepository<T, R>{

    default void softDelete(@Param("id") R id){
        Optional<T> optional=findById(id);
        if(optional.isEmpty())return;
        T t=optional.get();
        t.setDeleted(true);
        save(t);
    }

    default void softDeleteAll(@Param("id") Iterable<T> iterable){
        for(T t:iterable) {
            t.setDeleted(true);
        }
        saveAll(iterable);
    }

    default Pageable getPageable(Integer page, Integer pageSize){
        if(page==null||pageSize==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Page size or page number cannot be null");
        if(page<0||pageSize<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Page size or page number cannot be less than zero");
        if(pageSize>200)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Page size cannot be greater than 200");

        return PageRequest.of(page,pageSize, Sort.by("id").ascending());
    }
}

