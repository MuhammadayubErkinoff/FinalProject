package com.example.chorvoqgisbackend.controllers.geoserver;

import com.example.chorvoqgisbackend.service.geoserver.GeoserverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/layers")
@CrossOrigin()
public class GeoserverController {
    @Autowired
    private GeoserverService geoserverService;

    @GetMapping("/wms/{layer}")
    ResponseEntity<byte[]> wms(@RequestParam Map<String, String> params, @PathVariable("layer") String layer){

        return ResponseEntity.ok().header("Content-Type","image/png")
                .body(geoserverService.getWMS(params,layer));
    }

    @GetMapping("/wfs/{layer}")
    ResponseEntity<String> wfs(@RequestParam Map<String, String> params, @PathVariable("layer") String layer){

        return ResponseEntity.ok().header("Content-Type","application/json")
                .body(geoserverService.getWFS(params, layer));
    }

    @GetMapping("/wmts/{z}/{x}/{y}")
    ResponseEntity<byte[]> wmts(@PathVariable("x") Long x, @PathVariable("y") Long y, @PathVariable("z") Long z){

        return ResponseEntity.ok()
                .header("Content-Type","image/jpeg")
                .body(geoserverService.getTile(x, y, z));
    }

}

