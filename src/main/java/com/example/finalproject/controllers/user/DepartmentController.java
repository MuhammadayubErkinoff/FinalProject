package com.example.finalproject.controllers.user;


import com.example.finalproject.models.dto.Batch;
import com.example.finalproject.models.user.Department;
import com.example.finalproject.service.user.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin()
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;



    @GetMapping
    ResponseEntity<Batch<Department>> getDepartment(@RequestParam Integer page, @RequestParam Integer pageSize, @RequestParam(required = false) String name){

        Batch<Department>departmentBatch=departmentService.getDepartments(page, pageSize, name);
        departmentBatch.setData(departmentBatch.getData().stream().peek(Department::hide).toList());

        return ResponseEntity.ok(departmentBatch);
    }



    @GetMapping("/{id}")
    ResponseEntity<Department>getDepartmentById(@PathVariable("id") Long id){

        Department department=departmentService.getDepartment(id);
        department.hide();

        return ResponseEntity.ok(department);
    }


    @PostMapping
    ResponseEntity<Void>newDepartment(@RequestBody Department department) throws URISyntaxException {

        Department newDepartment=departmentService.newDepartment(department);
        return ResponseEntity.created(new URI("/api/roles/"+newDepartment.getId())).build();
    }

    @PutMapping("/{id}")
    ResponseEntity<Department>updateDepartment(@PathVariable("id") Long id, @RequestBody Department department){

        Department newDepartment=departmentService.updateDepartment(id, department);
        newDepartment.hide();

        return ResponseEntity.ok(newDepartment);
    }


    @DeleteMapping("/{id}")
    ResponseEntity<Void>deleteDepartment(@PathVariable("id") Long id){

        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
