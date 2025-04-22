package com.example.finalproject.config.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SoftDeleteAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.example.chorvoqgisbackend.service.user.*(..))")

    public void modifyEntityBeforeMethod() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin=false;
//                authentication.getAuthorities().stream()
//                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            disableSoftDeleteFilter();
        } else {
            enableSoftDeleteFilter();
        }
    }

    public void enableSoftDeleteFilter(){
        Session session=entityManager.unwrap(Session.class);
        session.enableFilter("deletedFilter").setParameter("isDeleted", false);
    }
    public void disableSoftDeleteFilter(){
        Session session=entityManager.unwrap(Session.class);
        session.disableFilter("deletedFilter");
    }

}
