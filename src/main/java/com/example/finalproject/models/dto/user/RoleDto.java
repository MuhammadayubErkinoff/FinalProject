package com.example.finalproject.models.dto.user;
import lombok.Data;

import java.util.Set;

@Data
public class RoleDto {
    private String name;
    private Set<Long> actionIds;
}
