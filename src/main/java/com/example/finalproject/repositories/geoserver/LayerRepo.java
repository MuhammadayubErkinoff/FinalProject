package com.example.finalproject.repositories.geoserver;

import com.example.finalproject.models.geoserver.Layer;
import com.example.finalproject.models.geoserver.LayerStatus;
import com.example.finalproject.repositories.CustomRepo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LayerRepo extends CustomRepo<Layer, Long> {


    Optional<Layer> findLayerByName(String name);

    boolean existsByName(String name);

    boolean existsByStatus(LayerStatus status);
}
