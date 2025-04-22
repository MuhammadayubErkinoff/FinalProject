package com.example.chorvoqgisbackend.service.user;

import com.example.chorvoqgisbackend.config.db.SoftDeleteAspect;
import com.example.chorvoqgisbackend.models.dto.Batch;
import com.example.chorvoqgisbackend.models.dto.user.RoleDto;
import com.example.chorvoqgisbackend.models.user.Action;
import com.example.chorvoqgisbackend.models.user.Role;
import com.example.chorvoqgisbackend.repositories.user.RoleRepo;
import com.example.chorvoqgisbackend.repositories.user.UserRepo;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Service
public class RoleService {

    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ActionService actionService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SoftDeleteAspect softDeleteAspect;

    public Batch<Role>getRoles(Integer page, Integer pageSize, String name){

        Pageable pageable=roleRepo.getPageable(page,pageSize);
        enableFilter(name);

        Batch<Role>roleBatch=new Batch<>();

        Page<Role> roles=roleRepo.findAll(pageable);
        roleBatch.setData(roles.stream().toList());
        roleBatch.setCount(roles.getTotalElements());

        disableFilter();
        return roleBatch;
    }

    public Role getRole(Long id){

        Optional<Role> roleOptional=roleRepo.findById(id);
        if(roleOptional.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Role not found");
        Role role=roleOptional.get();
        return role;
    }

    public Role newRole(RoleDto roleDto){

        if(roleDto.getName()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role should include name");
        if(!isNameValid(roleDto.getName()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role with such name exists");

        if(roleDto.getActionIds()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request should include action ids");
        Set<Action> actions= new HashSet<>(actionService.getActions(roleDto.getActionIds(), null, null).getData());
        if(actions.size()<roleDto.getActionIds().size())throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action id list contains invalid ids or duplicates");

        Role role=new Role(roleDto.getName(), actions);
        role=roleRepo.save(role);
        return role;
    }

    public Role updateRole(Long id, RoleDto roleDto){

        Role oldRole=getRole(id);

        if(oldRole.getName().equals("ADMIN"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin role cannot be changed");
        if(roleDto.getName()!=null && !isNameValid(roleDto.getName()))oldRole.setName(roleDto.getName());

        if(roleDto.getActionIds()!=null) {
            Set<Action> actions = new HashSet<>(actionService.getActions(roleDto.getActionIds(), null, null).getData());
            if(actions.size()<roleDto.getActionIds().size())throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action id list contains invalid ids or duplicates");
            if (actions.size() == roleDto.getActionIds().size()) oldRole.setActions(actions);
        }

        oldRole=roleRepo.save(oldRole);
        return oldRole;
    }


    public void deleteRole(Long id){

        Role role=getRole(id);

        if(role.getName().equals("ADMIN"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin role cannot be changed");
        if(userRepo.existsByRoleId(id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are users with these role");

        roleRepo.deleteById(id);
    }


    public Boolean isNameValid(String name){

        if(name==null) return false;
        softDeleteAspect.disableSoftDeleteFilter();
        boolean ret=roleRepo.existsByName(name);
        softDeleteAspect.modifyEntityBeforeMethod();
        return !ret;
    }

    private void enableFilter(String name){

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("roleFilter")
                .setParameter("name", name);
    }

    private void disableFilter(){

        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("roleFilter");
    }
}
