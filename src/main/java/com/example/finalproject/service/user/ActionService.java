package com.example.finalproject.service.user;

import com.example.finalproject.models.BaseModel;
import com.example.finalproject.models.dto.Batch;
import com.example.finalproject.models.user.Action;
import com.example.finalproject.repositories.user.ActionRepo;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

@Service
public class ActionService {

    @Autowired
    private ActionRepo actionRepo;
    @Autowired
    private EntityManager entityManager;
    public Batch<Action>getActions(Integer page, Integer pageSize, String name, String allowedPath){

        Pageable pageable=actionRepo.getPageable(page,pageSize);
        enableFilter(name, allowedPath);

        Batch<Action>actionBatch=new Batch<>();

        Page<Action>actions=actionRepo.findAll(pageable);
        actionBatch.setData(actions.stream().toList());
        actionBatch.setCount(actions.getTotalElements());

        disableFilter();
        return actionBatch;
    }


    public Batch<Action> getActions(String name, String allowedPath){

        enableFilter(name, allowedPath);
        Batch<Action>actions=new Batch<>();
        actions.setData(actionRepo.findAll().stream().toList());
        disableFilter();
        return actions;
    }

    public Batch<Action> getActions(Set<Long> ids, String name, String allowedPath){

        enableFilter(name, allowedPath);
        Batch<Action>actions=new Batch<>();
        actions.setData(actionRepo.findAllById(ids).stream().toList());
        disableFilter();
        return actions;
    }

    public Action getAction(Long id){

        Optional<Action> actionOptional=actionRepo.findById(id);
        if(actionOptional.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Action not found");
        Action action=actionOptional.get();
        return action;
    }

//    public Action updateAction(Long id, Action action){
//
//        Action oldAction=getAction(id);
//
//        if(action.getDescription()!=null)oldAction.setDescription(action.getDescription());
//
//        return actionRepo.save(oldAction);
//    }

    private void enableFilter(String name, String allowedPath){
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("actionFilter")
                .setParameter("name", name)
                .setParameter("allowedPath", allowedPath);
    }

    private void disableFilter(){
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("actionFilter");
    }
}
