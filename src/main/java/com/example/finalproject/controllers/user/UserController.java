package com.example.chorvoqgisbackend.controllers.user;

import com.example.chorvoqgisbackend.models.dto.Batch;
import com.example.chorvoqgisbackend.models.dto.user.UserDto;
import com.example.chorvoqgisbackend.models.user.User;
import com.example.chorvoqgisbackend.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin()
public class UserController {


    @Autowired
    private UserService userService;

    @GetMapping
    ResponseEntity<Batch<User>> getUsers(@RequestParam Integer page, @RequestParam Integer pageSize,
                                         @RequestParam(required = false) String name, @RequestParam(required = false) Long roleId,
                                         @RequestParam(required = false) Long departmentId){

        Batch<User>userBatch=userService.getUsers(page, pageSize, name, roleId, departmentId);
        userBatch.setData(userBatch.getData().stream().peek(User::hide).toList());

        return ResponseEntity.ok(userBatch);
    }

    @GetMapping("/{id}")
    ResponseEntity<User>getUserById(@PathVariable("id") Long id){

        User user=userService.getUser(id);user.hide();

        return ResponseEntity.ok(user);
    }

    @PostMapping
    ResponseEntity<Void>newUser(@RequestBody UserDto userDto) throws URISyntaxException {

        User user=userService.newUser(userDto);
        return ResponseEntity.created(new URI("/api/users/"+user.getId())).build();
    }
    @PutMapping("/{id}")
    ResponseEntity<User>updateUser(@PathVariable("id") Long id, @RequestBody UserDto userDto){

        User user=userService.updateUser(id, userDto);
        user.hide();

        return ResponseEntity.ok(user);
    }


    @DeleteMapping("/{id}")
    ResponseEntity<Void>deleteUser(@PathVariable("id") Long id){

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

