package com.example.chorvoqgisbackend.service.user;


import com.example.chorvoqgisbackend.config.db.SoftDeleteAspect;
import com.example.chorvoqgisbackend.models.dto.Batch;
import com.example.chorvoqgisbackend.models.user.Department;
import com.example.chorvoqgisbackend.repositories.user.DepartmentRepo;
import com.example.chorvoqgisbackend.repositories.user.UserRepo;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepo departmentRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SoftDeleteAspect softDeleteAspect;

    public Batch<Department>getDepartments(Integer page, Integer pageSize, String name){

        Pageable pageable=departmentRepo.getPageable(page,pageSize);
        enableFilter(name);

        Batch<Department>departmentBatch=new Batch<>();

        Page<Department> departments=departmentRepo.findAll(pageable);
        departmentBatch.setData(departments.stream().toList());
        departmentBatch.setCount(departments.getTotalElements());

        disableFilter();
        return departmentBatch;
    }

    public Department getDepartment(Long id){

        Optional<Department> departmentOptional=departmentRepo.findById(id);
        if(departmentOptional.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Department not found");
        Department department=departmentOptional.get();
        return department;
    }

    public Department newDepartment(Department department){

        if(!isNameValid(department.getName()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department with such name exists");

        Department newDepartment=new Department(department.getName());
        newDepartment=departmentRepo.save(newDepartment);
        return newDepartment;
    }

    public Department updateDepartment(Long id, Department department){

        Department oldDepartment=getDepartment(id);

        if(department.getName()!=null && isNameValid(department.getName()))oldDepartment.setName(department.getName());

        oldDepartment=departmentRepo.save(oldDepartment);
        return oldDepartment;
    }



    public void deleteDepartment(Long id){

        if(userRepo.existsByDepartmentId(id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are users with these department");

        departmentRepo.deleteById(id);
    }


    public Boolean isNameValid(String name){

        if(name==null)return false;
        softDeleteAspect.disableSoftDeleteFilter();
        boolean ret=departmentRepo.existsByName(name);
        softDeleteAspect.modifyEntityBeforeMethod();
        return !ret;
    }

    private void enableFilter(String name){

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("departmentFilter")
                .setParameter("name", name);
    }

    private void disableFilter(){

        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("departmentFilter");
    }
}
