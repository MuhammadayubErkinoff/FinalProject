package com.example.chorvoqgisbackend.controllers.geoserver;

import com.example.chorvoqgisbackend.models.geoserver.Layer;
import com.example.chorvoqgisbackend.service.geoserver.LayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/layers")
@CrossOrigin()
public class LayersController {

    @Autowired
    private LayerService layerService;

    @GetMapping()
    ResponseEntity<List<Layer>>getLayers(@RequestParam(required = false) List<Long>ids){

        if(ids==null)
            return ResponseEntity.ok(layerService.layers(null).stream().peek(Layer::hide).toList());

        return ResponseEntity.ok(layerService.findAllById(ids,null).stream().peek(Layer::hide).toList());
    }

    @GetMapping("/{id}")
    ResponseEntity<Layer>getLayer(@PathVariable("id") Long id){

        Layer layer=layerService.findLayerById(id);
        layer.hide();

        return ResponseEntity.ok(layer);
    }

    @GetMapping("/byName/{name}")
    ResponseEntity<Layer>getName(@PathVariable("name") String name){

        Layer layer=layerService.findLayerByName(name);
        layer.hide();

        return ResponseEntity.ok(layer);
    }

    @PostMapping("/new/{layerName}")
    ResponseEntity<Void>createNewLayer(@PathVariable String layerName, @RequestParam("file") MultipartFile file) throws URISyntaxException {

        Layer layer=layerService.newLayer(layerName, file);
        return ResponseEntity.created(new URI("/layers/"+layer.getId())).build();
    }

    @PostMapping("/finalize/{id}")
    ResponseEntity<Layer>finalizeLayer(@PathVariable("id") Long id, @RequestBody Layer layerDto){

        Layer layer=layerService.finalizeLayer(id, layerDto);
        layer.hide();
        return ResponseEntity.ok(layer);
    }

    @PutMapping("/{id}")
    ResponseEntity<Layer>updateLayer(@PathVariable("id") Long id, @RequestBody Layer layerDto){

        Layer layer=layerService.updateLayer(id, layerDto);
        layer.hide();
        return ResponseEntity.ok(layer);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void>deleteLayer(@PathVariable("id") Long id){

        layerService.deleteLayer(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/download/{layer}/{format}")
    public ResponseEntity<byte[]> exportLayer(@PathVariable String layer, @PathVariable String format) throws Exception {
        return layerService.downloadLayer(layer, format);
    }

}
