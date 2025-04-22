package com.example.chorvoqgisbackend.repositories.user;

import com.example.chorvoqgisbackend.models.user.Action;
import com.example.chorvoqgisbackend.repositories.CustomRepo;

import java.util.Optional;

public interface ActionRepo extends CustomRepo<Action,Long> {

    Boolean existsByName(String name);
    Optional<Action> findByName(String name);
}
