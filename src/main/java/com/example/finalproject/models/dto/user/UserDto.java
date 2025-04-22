package com.example.chorvoqgisbackend.models.dto.user;


import lombok.Data;

@Data
public class UserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Long roleId;
    private Long departmentId;
}
