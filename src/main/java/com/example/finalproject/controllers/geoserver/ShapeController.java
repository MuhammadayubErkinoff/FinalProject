package com.example.chorvoqgisbackend.controllers.geoserver;


import com.example.chorvoqgisbackend.models.geoserver.ShapeData;
import com.example.chorvoqgisbackend.service.geoserver.ShapeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/layers")
@CrossOrigin()
public class ShapeController {

    @Autowired
    private ShapeService shapeService;

    @GetMapping("/search")
    ResponseEntity<List<ShapeData>>search(@RequestParam List<Long>layerIds, @RequestParam String query){

        return ResponseEntity.ok(shapeService.search(layerIds,query));
    }

    @PostMapping("/shape")
    ResponseEntity<Void>addShape(@RequestBody ShapeData shapeData){

        shapeService.newShapeData(shapeData);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/shape")
    ResponseEntity<Void>deleteShape(@RequestBody ShapeData shapeData){

        shapeService.deleteShapeDate(shapeData);
        return ResponseEntity.noContent().build();
    }
}
