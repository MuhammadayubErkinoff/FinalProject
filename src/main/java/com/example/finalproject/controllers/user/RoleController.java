package com.example.finalproject.controllers.user;

import com.example.finalproject.models.dto.Batch;
import com.example.finalproject.models.dto.user.RoleDto;
import com.example.finalproject.models.user.Role;
import com.example.finalproject.service.user.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin()
public class RoleController {


    @Autowired
    private RoleService roleService;

    @GetMapping
    ResponseEntity<Batch<Role>> getRoles(@RequestParam Integer page, @RequestParam Integer pageSize, @RequestParam(required = false) String name){

        Batch<Role>roleBatch=roleService.getRoles(page, pageSize, name);
        roleBatch.setData(roleBatch.getData().stream().peek(Role::hide).toList());

        return ResponseEntity.ok(roleBatch);
    }

    @GetMapping("/{id}")
    ResponseEntity<Role>getRoleById(@PathVariable("id") Long id){

        Role role=roleService.getRole(id);
        role.hide();

        return ResponseEntity.ok(role);
    }

    @PostMapping
    ResponseEntity<Void>newRole(@RequestBody RoleDto roleDto) throws URISyntaxException {

        Role role=roleService.newRole(roleDto);
        return ResponseEntity.created(new URI("/api/roles/"+role.getId())).build();
    }
    @PutMapping("/{id}")
    ResponseEntity<Role>updateRole(@PathVariable("id") Long id, @RequestBody RoleDto roleDto){

        Role role=roleService.updateRole(id, roleDto);
        role.hide();

        return ResponseEntity.ok(role);
    }


    @DeleteMapping("/{id}")
    ResponseEntity<Void>deleteRole(@PathVariable("id") Long id){

        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
