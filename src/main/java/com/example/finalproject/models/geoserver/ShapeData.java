package com.example.finalproject.models.geoserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShapeData {
    private Integer id;
    private String layerName;
    private List<List<Double>>coordinates;
    private Map<String, Object>data;

    public static String parseCoordinates(List<List<Double>>coordinates){
        StringBuilder coords=new StringBuilder("SRID=4326;MULTIPOLYGON(((");

        if(coordinates==null || coordinates.isEmpty())
            throw new IllegalArgumentException("Shape data does not contain coordinates");
        if(coordinates.size()<4 || !coordinates.get(0).equals(coordinates.get(coordinates.size()-1)))
            throw new IllegalArgumentException( "Invalid Shape Coordinates");
        for(List<Double>coordinate : coordinates){
            if(coordinate.size()!=2)
                throw new IllegalArgumentException("Invalid Shape Coordinates");
            if(!coords.toString().equals("SRID=4326;MULTIPOLYGON((("))coords.append(",");
            coords.append(coordinate.get(0))
                    .append(" ")
                    .append(coordinate.get(1));
        }
        coords.append(")))");

        return coords.toString();
    }
}
