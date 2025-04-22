package com.example.finalproject.models.geoserver;


import com.example.finalproject.models.BaseModel;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Layer extends BaseModel {

    @Column(unique = true, nullable = false)
    private String name;
    private String title;
    private String geometryField;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Field>fields;
    private Boolean isSearchable;
    private Boolean isBackground;
    private Integer minZoom;
    private Integer maxZoom;
    private String color;
    private LayerStatus status;
    private String failCause;

    @Override
    public void hide() {
        super.hide();
        fields=fields.stream().peek(field -> field.setId(null)).toList();
    }

    public static boolean isValidLayerName(String name){
        if(!Character.isLetter(name.charAt(0)))return false;
        for(Character c: name.toCharArray()){
            if(!Character.isLetterOrDigit(c)&&c!='_'){
                return false;
            }
        }
        return true;
    }
}


