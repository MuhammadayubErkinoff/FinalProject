package com.example.finalproject.service.auth;

import com.example.finalproject.models.dto.LoginDto;
import com.example.finalproject.models.user.User;
import com.example.finalproject.repositories.user.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private  UserRepo userRepo;
    @Autowired
    private  AuthenticationManager authenticationManager;

    public User authenticate(LoginDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getLogin(),
                        input.getPassword()
                )
        );

        return userRepo.findByLogin(input.getLogin())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Login or password is wrong!"));
    }

    public User me(){

        Optional<User>user=userRepo.findByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user.isEmpty())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found");
        return user.get();
    }

    public User changeMe(User user){

        User me=me();
        if(user.getFirstName()!=null)me.setFirstName(user.getFirstName());
        if(user.getLastName()!=null)me.setLastName(user.getLastName());
        if(user.getPassword()!=null)me.setPassword(user.getPassword());

        return userRepo.save(me);
    }
}

