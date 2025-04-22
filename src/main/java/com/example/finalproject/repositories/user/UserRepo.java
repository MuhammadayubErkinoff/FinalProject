package com.example.chorvoqgisbackend.repositories.user;

import com.example.chorvoqgisbackend.models.user.User;
import com.example.chorvoqgisbackend.repositories.CustomRepo;

import java.util.Optional;

public interface UserRepo extends CustomRepo<User,Long> {
    Optional<User>findByLogin(String login);
    Boolean existsByRoleId(Long roleId);
    Boolean existsByLogin(String login);
    Boolean existsByDepartmentId(Long departmentId);
}

