package com.example.finalproject.config.security;

import com.example.finalproject.models.user.Action;
import com.example.finalproject.models.user.User;
import com.example.finalproject.repositories.user.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

@Component
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Autowired
    private UserRepo userRepository;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext requestContext) {
        Authentication authentication = authenticationSupplier.get();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<Action> userActions = user.getRole().getActions();


        return new AuthorizationDecision(hasPermission(userActions, requestContext.getRequest()));
    }

    private boolean hasPermission(Set<Action>userActions, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        System.out.println(requestMethod+": "+requestURI);

        for(Action action:userActions){
            if(requestURI.startsWith(action.getAllowedPath())){
                if(requestMethod.equals("HEAD") ||
                        requestMethod.equals("OPTIONS") ||
                        action.getAllowedMethods().contains(requestMethod) ||
                        action.getAllowedMethods().contains("*")) {
                    return true;
                }
            }
        }

        return false;
    }
}
