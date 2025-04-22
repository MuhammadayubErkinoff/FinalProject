package com.example.finalproject.repositories.user;

import com.example.finalproject.models.user.User;
import com.example.finalproject.repositories.CustomRepo;

import java.util.Optional;

public interface UserRepo extends CustomRepo<User,Long> {
    Optional<User>findByLogin(String login);
    Boolean existsByRoleId(Long roleId);
    Boolean existsByLogin(String login);
    Boolean existsByDepartmentId(Long departmentId);
}

