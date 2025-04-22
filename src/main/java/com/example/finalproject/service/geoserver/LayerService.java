package com.example.finalproject.service.geoserver;


import com.example.finalproject.models.geoserver.Field;
import com.example.finalproject.models.geoserver.Layer;
import com.example.finalproject.models.geoserver.LayerStatus;
import com.example.finalproject.repositories.geoserver.LayerRepo;
import com.example.finalproject.utils.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.*;

@Service
public class LayerService {

    @Autowired
    private LayerRepo layerRepo;
    @Autowired
    private GeoserverService geoserverService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Value("${postgis.host}") private String POSTGIS_HOST;
    @Value("${postgis.db}") private String POSTGIS_DB;
    @Value("${spring.datasource.username}") private String POSTGIS_USER;
    @Value("${spring.datasource.password}") private String POSTGIS_PASSWORD;

    //TODO: add filtering
    public List<Layer>layers(String name){

        return layerRepo.findAll();
    }

    public Set<Layer>findAllById(List<Long>ids, String name){

        return new HashSet<>(layerRepo.findAllById(ids));
    }

    public Layer findLayerById(Long id){

        Optional<Layer>layerOptional=layerRepo.findById(id);
        if(layerOptional.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Layer is not found");
        return layerOptional.get();
    }

    public Layer findLayerByName(String name){

        Optional<Layer>layerOptional=layerRepo.findLayerByName(name);
        if(layerOptional.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Layer is not found");
        return layerOptional.get();
    }

    public Layer newLayer(String layerName, MultipartFile file){

        if(!Layer.isValidLayerName(layerName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Layer name should start with letter and only consist of letters,digits, and underscores");
        }
        if(layerRepo.existsByName(layerName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Layer with such name exists");
        }
        if (layerRepo.existsByStatus(LayerStatus.PENDING) || layerRepo.existsByStatus(LayerStatus.READY_FOR_CREATION)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Another layer is in the process of the creation");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        if (!FileService.isZipFile(file)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not a zip file");
        }

        Layer layer=new Layer();
        layer.setName(layerName);
        layer.setTitle(layerName);
        layer.setStatus(LayerStatus.PENDING);
        layer.setIsBackground(false);
        layer=layerRepo.save(layer);

        Layer finalLayer = layer;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new Thread(() -> {
            try {
                createLayer(finalLayer, file, authentication.getName());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        return finalLayer;
    }

    public Layer finalizeLayer(Long id, Layer layerDto){
        Layer layer=findLayerById(id);

        if(layer.getStatus()==LayerStatus.CREATION_FAILED)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This layer was failed");
        if(layer.getStatus()==LayerStatus.PENDING)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This layer is pending");
        if(layer.getStatus()==LayerStatus.CREATED)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This layer is already created");

        if(layerDto.getFields()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fields are null");
        if(layerDto.getIsSearchable()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IsSearchable is null");
        if(layerDto.getMinZoom()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Min zoom are null");
        if(layerDto.getMaxZoom()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max zoom are null");
        if(layerDto.getColor()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Color are null");

        HashMap<String, Field>map=new HashMap<>();
        layerDto.getFields().forEach(field -> map.put(field.getName(),field));

        List<Field>fields=new ArrayList<>();

        for(Field field:layer.getFields()){
            if(!map.containsKey(field.getName()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Layer should include "+field.getName()+" field");
            Field fieldDto=map.get(field.getName());
            if(fieldDto.getIsMandatory()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isMandatory is null for "+field.getName()+" field");
            if(fieldDto.getIsSearchable()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isSearchable is null for "+field.getName()+" field");
            if(fieldDto.getIsActive()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isActive is null for "+field.getName()+" field");

            field.setIsMandatory(fieldDto.getIsMandatory());
            field.setIsSearchable(fieldDto.getIsSearchable());
            field.setIsActive(fieldDto.getIsActive());
            if(fieldDto.getTitle()!=null) field.setTitle(fieldDto.getTitle());

            fields.add(field);
        }

        layer.setFields(fields);
        layer.setIsSearchable(layerDto.getIsSearchable());
        layer.setMinZoom(layerDto.getMinZoom());
        layer.setMaxZoom(layerDto.getMaxZoom());
        layer.setColor(layerDto.getColor());
        layer.setStatus(LayerStatus.CREATED);
        if(layerDto.getIsBackground()!=null)layer.setIsBackground(layerDto.getIsBackground());
        if(layerDto.getTitle()!=null)layer.setTitle(layerDto.getTitle());

        return layerRepo.save(layer);
    }

    public Layer updateLayer(Long id, Layer layer){

        Layer oldLayer=findLayerById(id);

        if (oldLayer.getStatus()==LayerStatus.PENDING || oldLayer.getStatus()==LayerStatus.READY_FOR_CREATION){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This layer is not created yet");
        }
        if(oldLayer.getStatus()==LayerStatus.CREATION_FAILED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This layer was failed");

        if(layer.getFields()!=null){
            HashMap<String, Field>map=new HashMap<>();
            layer.getFields().forEach(field -> map.put(field.getName(),field));

            List<Field>fields=new ArrayList<>();


            for(Field field:oldLayer.getFields()){
                if(map.containsKey(field.getName())){

                    Field newField=map.get(field.getName());
                    if(newField.getIsMandatory()!=null)field.setIsMandatory(newField.getIsMandatory());
                    if(newField.getIsSearchable()!=null)field.setIsSearchable(newField.getIsSearchable());
                    if(newField.getIsActive()!=null)field.setIsActive(newField.getIsActive());
                    if(newField.getTitle()!=null)field.setTitle(newField.getTitle());
                }
                fields.add(field);
            }
            oldLayer.setFields(fields);
        }
        if(layer.getTitle()!=null)oldLayer.setTitle(layer.getTitle());
        if(layer.getIsSearchable()!=null)oldLayer.setIsSearchable(layer.getIsSearchable());
        if(layer.getIsBackground()!=null)oldLayer.setIsBackground(layer.getIsBackground());
        if(layer.getMinZoom()!=null)oldLayer.setMinZoom(layer.getMinZoom());
        if(layer.getMaxZoom()!=null)oldLayer.setMaxZoom(layer.getMaxZoom());
        if(layer.getColor()!=null)oldLayer.setColor(layer.getColor());

        return layerRepo.save(oldLayer);
    }

    public void deleteLayer(Long id){

        Layer layer=findLayerById(id);
        deleteLayerTable(layer.getName());
        geoserverService.deleteLayer(layer.getName());
        layerRepo.delete(layer);
    }

    public ResponseEntity<byte[]> downloadLayer(String layer, String format) throws IOException {
        List<String> columns = jdbcTemplate.query(
                "SELECT column_name FROM information_schema.columns WHERE table_name = ? ORDER BY ordinal_position",
                (rs, rowNum) -> rs.getString("column_name"), layer
        );

        String selectQuery = "SELECT " + String.join(", ", columns) + " FROM " + layer;

        List<Object[]> rows = jdbcTemplate.query(selectQuery,
                (ResultSet rs, int rowNum) -> {
                    Object[] row = new Object[columns.size()];
                    for (int i = 0; i < columns.size(); i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    return row;
                });

        byte[] data;
        HttpHeaders headers = new HttpHeaders();

        switch (format.toLowerCase()) {
            case "json":
                data = FileService.exportToJson(columns, rows);
                headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + layer + ".json\"");
                break;
            case "csv":
                data = FileService.exportToCsv(columns, rows);
                headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + layer + ".csv\"");
                break;
            case "xlsx":
                data = FileService.exportToXlsx(columns, rows);
                headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + layer + ".xlsx\"");
                break;
            case "shp":
                data = FileService.exportToShapefile(layer);
                headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + layer + ".zip\"");
                break;
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported format".getBytes());
        }
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }


    //TODO: check existence of other needed files
    private void createLayer(Layer layer, MultipartFile file, String username) throws InterruptedException {

        String uploadPath="/shapefiles/"+layer.getName();
        Path zipFilePath;

        //Uploading file
        try {
            zipFilePath = FileService.uploadFile(uploadPath, file);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            layer.setStatus(LayerStatus.CREATION_FAILED);
            layer.setFailCause("Error while uploading");
            layerRepo.save(layer);
            FileService.deleteFolder(uploadPath);
            return;
        }

        //Extracting
        try {
            FileService.unzipFile(zipFilePath.toString(), uploadPath);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            layer.setStatus(LayerStatus.CREATION_FAILED);
            layer.setFailCause("Error while extraction zip file");
            layerRepo.save(layer);
            FileService.deleteFolder(uploadPath);
            return;
        }

        System.out.println(FileService.listFilesInDirectory(uploadPath));
        //Checking if shapefile exists
        if(FileService.numberOfShpFiles(uploadPath)==0) {
            layer.setStatus(LayerStatus.CREATION_FAILED);
            layer.setFailCause("Shapefile does not exist");
            layerRepo.save(layer);
            FileService.deleteFolder(uploadPath);
            return;
        }
        if(FileService.numberOfShpFiles(uploadPath)>1) {
            layer.setStatus(LayerStatus.CREATION_FAILED);
            layer.setFailCause("More than one shapefile exist");
            layerRepo.save(layer);
            FileService.deleteFolder(uploadPath);
            return;
        }
        String shpFilePath = FileService.getShapefilePath(uploadPath);

        //Adding layer to Postgis
        boolean success = uploadShapefileToPostGIS(shpFilePath, layer.getName());
        if (success){
            try {
                addAuditColumnsIfNotExist(layer.getName());
                updateAuditFields(layer.getName(), username);
            }
            catch (Exception e){
                System.out.println(e);
                FileService.deleteFolder(uploadPath);
                layer.setStatus(LayerStatus.CREATION_FAILED);
                layer.setFailCause("Failed to upload shapefile to database");
                layerRepo.save(layer);
                return;
            }

            FileService.deleteFolder(uploadPath);
            List<Map<String, Object>>columns=getTableColumns(layer.getName());
            List<Field>fields=new ArrayList<>();
            for(Map<String,Object>column: columns){
                if (column.get("data_type").equals("USER-DEFINED")) {
                    layer.setGeometryField(column.get("column_name").toString());
                }
                else if(!column.get("column_name").equals("gid")){
                    Field field=new Field();
                    field.setName(column.get("column_name").toString());
                    field.setTitle(column.get("column_name").toString());
                    field.setType(column.get("data_type").toString());
                    fields.add(field);
                }
            }
            layer.setFields(fields);
            layerRepo.save(layer);
            System.out.println("Shapefile uploaded to PostGIS successfully");
        }
        else{
            FileService.deleteFolder(uploadPath);
            layer.setStatus(LayerStatus.CREATION_FAILED);
            layer.setFailCause("Failed to upload shapefile to database");
            layerRepo.save(layer);
            return;
        }

        Thread.sleep(2000);

        //Adding layer to geoserver
        geoserverService.publishLayer(layer);
        if(geoserverService.checkLayerExists(layer.getName())){
            System.out.println(layer.getName()+" exists in geoserver");
            layer.setStatus(LayerStatus.READY_FOR_CREATION);
            layerRepo.save(layer);
        }
        else{
            System.out.println(layer.getName()+" does not exist in geoserver");
            layer.setStatus(LayerStatus.CREATION_FAILED);
            layer.setFailCause("Failed to publish layer");
            layerRepo.save(layer);
        }
    }


    private boolean uploadShapefileToPostGIS(String shpFilePath, String layerName) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "sh", "-c",
                    String.format("shp2pgsql -s 4326 %s public.%s | PGPASSWORD=\"%s\" psql -h %s -U %s -d %s",
                            shpFilePath, layerName, POSTGIS_PASSWORD, POSTGIS_HOST, POSTGIS_USER, POSTGIS_DB)
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getTableColumns(String tableName) {
        String sql = "SELECT column_name, data_type " +
                "FROM information_schema.columns " +
                "WHERE table_name = ? AND table_schema = 'public'";

        return jdbcTemplate.queryForList(sql, tableName);
    }


    public void deleteLayerTable(String layerName) {
        String sql = "DROP TABLE IF EXISTS public." + layerName + " CASCADE;";
        jdbcTemplate.execute(sql);
        System.out.println("Layer table deleted: " + layerName);
    }

    public void addAuditColumnsIfNotExist(String tableName) {
        String addCreatedBy = "ALTER TABLE " + tableName + " ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);";
        String addCreatedAt = "ALTER TABLE " + tableName + " ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT now();";

        jdbcTemplate.execute(addCreatedBy);
        jdbcTemplate.execute(addCreatedAt);
    }

    public void updateAuditFields(String tableName, String username) {
        String update = "UPDATE " + tableName + " SET created_by = ? WHERE created_by IS NULL;";
        jdbcTemplate.update(update, username);
    }


}

