package com.example.finalproject.repositories.user;

import com.example.finalproject.models.user.Action;
import com.example.finalproject.repositories.CustomRepo;

import java.util.Optional;

public interface ActionRepo extends CustomRepo<Action,Long> {

    Boolean existsByName(String name);
    Optional<Action> findByName(String name);
}
