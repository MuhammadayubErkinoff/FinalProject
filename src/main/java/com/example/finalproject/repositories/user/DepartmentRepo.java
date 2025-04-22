package com.example.chorvoqgisbackend.repositories.user;

import com.example.chorvoqgisbackend.models.user.Department;
import com.example.chorvoqgisbackend.repositories.CustomRepo;

import java.util.Optional;

public interface DepartmentRepo extends CustomRepo<Department, Long> {

    Optional<Department>findByName(String name);
    Boolean existsByName(String name);
}
