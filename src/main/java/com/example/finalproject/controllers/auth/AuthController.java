package com.example.finalproject.controllers.auth;


import com.example.finalproject.models.dto.LoginDto;
import com.example.finalproject.models.dto.LoginResponseDto;
import com.example.finalproject.models.user.User;
import com.example.finalproject.service.auth.AuthService;
import com.example.finalproject.service.auth.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin()
public class AuthController {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthService authenticationService;

    @PostMapping("/login")
    ResponseEntity<LoginResponseDto>login(@RequestBody LoginDto loginDto){
        User authenticatedUser = authenticationService.authenticate(loginDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponseDto loginResponse = new LoginResponseDto();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/me")
    ResponseEntity<User>me(){

        User me=authenticationService.me();
        me.hide();
        return ResponseEntity.ok(me);
    }

    @PutMapping("/me")
    ResponseEntity<User>changeMe(@RequestBody User user){

        User me=authenticationService.changeMe(user);
        me.hide();
        return ResponseEntity.ok(me);
    }

}

