package com.example.finalproject.repositories.user;

import com.example.finalproject.models.user.Department;
import com.example.finalproject.repositories.CustomRepo;

import java.util.Optional;

public interface DepartmentRepo extends CustomRepo<Department, Long> {

    Optional<Department>findByName(String name);
    Boolean existsByName(String name);
}
