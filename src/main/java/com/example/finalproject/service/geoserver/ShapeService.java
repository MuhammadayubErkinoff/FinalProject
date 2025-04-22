package com.example.chorvoqgisbackend.service.geoserver;


import com.example.chorvoqgisbackend.models.geoserver.ShapeData;
import com.example.chorvoqgisbackend.models.geoserver.Field;
import com.example.chorvoqgisbackend.models.geoserver.Layer;
import com.example.chorvoqgisbackend.models.geoserver.LayerStatus;
import com.example.chorvoqgisbackend.repositories.geoserver.ShapeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ShapeService {



    @Autowired
    private ShapeRepo shapeRepo;
    @Autowired
    private LayerService layerService;

    public List<ShapeData> search(List<Long> layerIds, String query) {

        if (query == null || query.length()==0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "query must not be empty");
        Set<Layer> layers=layerService.findAllById(layerIds, null);
        for(Layer layer:layers){
            if(layer.getStatus()!= LayerStatus.CREATED)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, layer.getName()+" is not fully created");
            if(!layer.getIsSearchable())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, layer.getName()+" is not searchable");
        }
        if(layers.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "layerIds does not contain any real layer id");

        ArrayList<ShapeData> shapeDataList=new ArrayList<>();
        Integer cnt=(4+layers.size())/layers.size();
        for(Layer layer: layers){
            shapeDataList.addAll(shapeRepo.searchShapes(layer, query, cnt));
        }

        return shapeDataList;
    }

    public void newShapeData(ShapeData shapeData){

        Layer layer=layerService.findLayerByName(shapeData.getLayerName());
        if(layer.getStatus()!= LayerStatus.CREATED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, layer.getName()+" is not fully created");
        Map<String, Object> data=new HashMap<>();

        try{data.put(layer.getGeometryField(),ShapeData.parseCoordinates(shapeData.getCoordinates()));}
        catch (IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());}

        for(Field field:layer.getFields()) {
            if(field.getIsActive()) {
                if (shapeData.getData().containsKey(field.getName()))
                    data.put(field.getName(), shapeData.getData().get(field.getName()));
                else if (field.getIsMandatory())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shape data does not contain mandatory fields");
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        data.put("created_by", authentication.getName());
        data.remove("created_at");

        shapeRepo.insertShape(shapeData.getLayerName(), layer.getGeometryField(), data);
    }

    public void deleteShapeDate(ShapeData shapeData){

        Layer layer=layerService.findLayerByName(shapeData.getLayerName());
        if(layer.getStatus()!= LayerStatus.CREATED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, layer.getName()+" is not fully created");
        try {
            shapeRepo.deleteShape(layer.getName(), shapeData.getId());
        }
        catch (IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());}
    }
}
