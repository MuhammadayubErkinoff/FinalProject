package com.example.finalproject.repositories.user;

import com.example.finalproject.models.user.Role;
import com.example.finalproject.repositories.CustomRepo;

import java.util.Optional;

public interface RoleRepo  extends CustomRepo<Role,Long> {

    Boolean existsByName(String name);

    Optional<Role> findByName(String name);
}
