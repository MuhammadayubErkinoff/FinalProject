package com.example.finalproject.service.geoserver;


import com.example.finalproject.models.geoserver.Layer;
import com.example.finalproject.models.geoserver.LayerStatus;
import com.example.finalproject.repositories.geoserver.LayerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class GeoserverService {

    @Value("${geoserver.uri}")
    private String GEOSERVER_URL;
    @Value("${geoserver.workspace}")
    private String WORKSPACE;
    @Value("${geoserver.datastore}")
    private String DATASTORE;
    @Value("${geoserver.user}")
    private String USERNAME;
    @Value("${geoserver.password}")
    private String PASSWORD;
    @Value("${wmts.uri}")
    private String WMTS;
    @Value("${wmts.key}")
    private String WMTS_KEY;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LayerRepo layerRepo;


    public byte[] getWMS(Map<String, String> params, String layerName) {


        Optional<Layer> layerOptional = layerRepo.findLayerByName(layerName);
        if (layerOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such layer does not exist");
        Layer layer=layerOptional.get();
        if(layer.getStatus()!= LayerStatus.CREATED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, layer.getName()+" is not fully created");

        if (!params.containsKey("WIDTH"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query should contain WIDTH");
        if (!params.containsKey("HEIGHT"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query should contain HEIGHT");
        if (!params.containsKey("BBOX"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query should contain BBOX");

        String[] points = params.get("BBOX").split(",");
        if (points.length != 4) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid BBOX");
        String bbox = points[1] + "," + points[0] + "," + points[3] + "," + points[2];

        URI uri = UriComponentsBuilder.fromUriString(GEOSERVER_URL + "/chorvoq-gis/wms")
                .queryParam("SERVICE", "WMS")
                .queryParam("VERSION", "1.1.1")
                .queryParam("REQUEST", "GetMap")
                .queryParam("FORMAT", "image/png")
                .queryParam("SRS", "EPSG:4326")
                .queryParam("LAYERS", "chorvoq-gis:" + layer.getName())
                .queryParam("WIDTH", params.get("WIDTH"))
                .queryParam("HEIGHT", params.get("HEIGHT"))
                .queryParam("SLD", params.get("SLD"))
                .queryParam("SLD_BODY", params.get("SLD_BODY"))
                .queryParam("BBOX", bbox)
                .queryParamIfPresent("TRANSPARENT", Optional.ofNullable(params.get("TRANSPARENT")))
                .queryParamIfPresent("STYLES", Optional.ofNullable(params.get("STYLES")))
                .queryParamIfPresent("exceptions", Optional.ofNullable(params.get("exceptions")))
                .build()
                .encode()
                .toUri();

        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
        if (response.getStatusCode() != HttpStatus.OK) throw new ResponseStatusException(response.getStatusCode(), "");

        return response.getBody();
    }

    public String getWFS(Map<String, String> params, String layerName) {

        Optional<Layer> layerOptional = layerRepo.findLayerByName(layerName);
        if (layerOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such layer does not exist");
        Layer layer=layerOptional.get();
        if(layer.getStatus()!= LayerStatus.CREATED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, layer.getName()+" is not fully created");

        if (!params.containsKey("bbox") && (!params.containsKey("lat") || !params.containsKey("lon")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query should contain bbox or lat/lon");

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(GEOSERVER_URL + "/chorvoq-gis/wfs")
                .queryParam("service", "WFS")
                .queryParam("version", "1.1.0")
                .queryParam("request", "GetFeature")
                .queryParam("outputFormat", "application/json")
                .queryParam("srsName", "EPSG:4326")
                .queryParam("typeName", "chorvoq-gis:" + layer.getName());

        if (params.containsKey("lat") && params.containsKey("lon")) {
            double lon, lat;
            try {
                lon = Double.parseDouble(params.get("lon"));
                lat = Double.parseDouble(params.get("lat"));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat/lon in wrong format");
            }
            String filter = "<Filter><Intersects><PropertyName>geom</PropertyName><Point><coordinates>" +
                    lat + "," + lon + "</coordinates></Point></Intersects></Filter>";
            uriBuilder.queryParam("FILTER", filter);
        } else {
            String[] points = params.get("bbox").split(",");
            if (points.length != 4) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bbox");
            String bbox = points[1] + "," + points[0] + "," + points[3] + "," + points[2];

            uriBuilder.queryParam("bbox", bbox);
        }

        URI uri = uriBuilder.build().encode().toUri();

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, String.class);
        return response.getBody();
    }

    public byte[] getTile(Long x, Long y, Long z) {

        URI uri = UriComponentsBuilder.fromUriString(WMTS + "/" + z + "/" + x + "/" + y + ".jpg")
                .queryParam("key", WMTS_KEY)
                .build()
                .encode()
                .toUri();

        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
        return response.getBody();
    }

    public void publishLayer(Layer layer) {
        reloadStore();
        String url = GEOSERVER_URL + "/rest/workspaces/" + WORKSPACE + "/datastores/" + DATASTORE + "/featuretypes";

        String xmlPayload = "<featureType>" +
                "<name>" + layer.getName() + "</name>" +
                "<nativeName>" + layer.getName() + "</nativeName>" +
                "<title>" + layer.getName() + "</title>" +
                "<srs>EPSG:4326</srs>" +
                "</featureType>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setBasicAuth(USERNAME, PASSWORD);

        HttpEntity<String> requestEntity = new HttpEntity<>(xmlPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                layer.setStatus(LayerStatus.CREATION_FAILED);
                layer.setFailCause("Failed to publish layer");
                layerRepo.save(layer);
            }
        }
        catch (Exception e){
            layer.setStatus(LayerStatus.CREATION_FAILED);
            layer.setFailCause("Failed to publish layer");
            layerRepo.save(layer);
        }
    }

    public boolean checkLayerExists(String layerName) {
        reloadStore();
        String url = GEOSERVER_URL + "/rest/layers/"  + layerName + ".json";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(USERNAME, PASSWORD);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            System.out.println("checkLayerExists for "+layerName+": "+response);
            return response.getStatusCode().is2xxSuccessful();
        }
        catch (Exception e){
            return false;
        }
    }

    public void deleteLayer(String layerName) {

        if(!checkLayerExists(layerName))return;
        String url = GEOSERVER_URL + "/rest/workspaces/" + WORKSPACE + "/datastores/" + DATASTORE + "/featuretypes/" + layerName + "?recurse=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(USERNAME, PASSWORD);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Layer deleted successfully!");
        } else {
            System.out.println("Failed to delete layer: " + response.getBody());
        }
    }

    public void reloadStore() {
        try {
            String reloadUrl = String.format("%s/rest/workspaces/%s/datastores/%s/reload", GEOSERVER_URL, WORKSPACE, DATASTORE);
            URL url = new URL(reloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set method to POST
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Add basic auth header
            String basicAuth = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());
            connection.setRequestProperty("Authorization", "Basic " + basicAuth);
            connection.setRequestProperty("Content-Type", "text/plain");

            // Connect and get response
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ Store reloaded successfully.");
            } else {
                System.out.println("❌ Failed to reload store. Response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}