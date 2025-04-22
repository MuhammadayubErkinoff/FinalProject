package com.example.chorvoqgisbackend.repositories.geoserver;

import com.example.chorvoqgisbackend.models.geoserver.ShapeData;
import com.example.chorvoqgisbackend.models.geoserver.Field;
import com.example.chorvoqgisbackend.models.geoserver.Layer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.List;

@Repository
public class ShapeRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @PersistenceContext
    private EntityManager entityManager;


    public List<ShapeData>searchShapes(Layer layer, String searchTerm, Integer cnt){
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        for (Field field : layer.getFields()) {
            sql.append(field.getName()).append(", ");
        }
        sql.append("gid, ");
        sql.append(layer.getGeometryField()).append(" FROM ").append(layer.getName()).append(" WHERE ");

        boolean isSearchable=false;

        for (int i = 0; i < layer.getFields().size(); i++) {
            Field field = layer.getFields().get(i);
            if(field.getIsActive()) {
                if (field.getIsSearchable()) {
                    if (isSearchable) sql.append("OR ");
                    sql.append(field.getName()).append("::TEXT ILIKE :searchTerm ");
                    isSearchable = true;
                }
            }
        }
        sql.append("LIMIT ").append(cnt);

        if (!isSearchable)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, layer.getName()+" does not contain any searchable fields");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        List<Object[]> properties = query.getResultList();

        List<ShapeData> shapes = new ArrayList<>();

        for (Object[] property : properties) {
            Map<String, Object> data = new HashMap<>();
            for (int j = 0; j < property.length - 2; j++) {
                Field curField = layer.getFields().get(j);
                if(curField.getIsActive()) {
                    data.put(curField.getName(), property[j]);
                }
            }
            data.put(layer.getGeometryField(), property[property.length-1].toString());

            Integer gid = Integer.parseInt(property[property.length-2].toString());
            shapes.add(new ShapeData(gid, layer.getName(),null, data));
        }

        return shapes;
    }

    public void insertShape(String tableName, String geomName, Map<String, Object> data) {

        if (data.isEmpty()) {
            throw new IllegalArgumentException("No data provided for insertion");
        }

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        Object[] values = new Object[data.size()];

        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            columns.add(entry.getKey());
            if (entry.getKey().equalsIgnoreCase(geomName)) {
                placeholders.add("ST_GeomFromEWKT(?)");
            } else {
                placeholders.add("?");
            }
            values[i++] = entry.getValue();
        }

        String sql = String.format("INSERT INTO %s (%s, created_at) VALUES (%s, NOW())", tableName, columns, placeholders);


        jdbcTemplate.update(sql, values);
    }

    public boolean exists(String tableName, Integer gid){
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE gid = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, gid);

        return count > 0;
    }

    public void deleteShape(String tableName, Integer gid) {
        if(!exists(tableName, gid))throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shape not fonud");

        Object[] values = {gid};

        String sql = String.format("DELETE FROM %s WHERE gid = ?", tableName);
        jdbcTemplate.update(sql, values);
    }
}