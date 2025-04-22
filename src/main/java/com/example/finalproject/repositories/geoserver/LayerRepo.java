package com.example.chorvoqgisbackend.repositories.geoserver;

import com.example.chorvoqgisbackend.models.geoserver.Layer;
import com.example.chorvoqgisbackend.models.geoserver.LayerStatus;
import com.example.chorvoqgisbackend.repositories.CustomRepo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LayerRepo extends CustomRepo<Layer, Long> {


    Optional<Layer> findLayerByName(String name);

    boolean existsByName(String name);

    boolean existsByStatus(LayerStatus status);
}
