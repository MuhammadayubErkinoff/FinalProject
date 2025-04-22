package com.example.finalproject.config.db;

import com.example.finalproject.models.user.Action;
import com.example.finalproject.models.user.Department;
import com.example.finalproject.models.user.Role;
import com.example.finalproject.models.user.User;
import com.example.finalproject.repositories.user.ActionRepo;
import com.example.finalproject.repositories.user.DepartmentRepo;
import com.example.finalproject.repositories.user.RoleRepo;
import com.example.finalproject.repositories.user.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DbStartup {

    @Autowired
    RoleRepo roleRepo;
    @Autowired
    ActionRepo actionRepo;
    @Autowired
    DepartmentRepo departmentRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner commandLineRunner(){
        return args -> {

            Action seeingMap = addAction("Xaritani ko'rish", "Xaritani ko'rishga ruxsat", "/layers", Set.of("GET"));
            Action mapManagement = addAction("Map Management", "Map Management", "/layers", Set.of("*"));
            Action seeingAction = addAction("Actions", "Seeing actions", "/api/actions", Set.of("*"));
            Action seeingRoles = addAction("Seeing roles", "Seeing roles", "/api/roles", Set.of("GET"));
            Action roleManagement = addAction("Role Management", "Seeing, Updating, Adding, Deleting roles", "/api/roles", Set.of("*"));
            Action seeingUsers = addAction("Seeing users", "Seeing users", "/api/users", Set.of("GET"));
            Action userManagement = addAction("Users Management", "Seeing, Updating, Adding, Deleting users", "/api/users", Set.of("*"));
            Action seeingDepartments = addAction("Seeing Departments", "Seeing Departments", "/api/departments", Set.of("GET"));
            Action departmentsManagement = addAction("Departments Management", "Seeing, Updating, Adding, Deleting Departments", "/api/departments", Set.of("*"));
            Action seeingInstructions = addAction("Seeing Instructions", "Seeing Instructions", "/api/instructions", Set.of("GET"));
            Action cmsManagement = addAction("CMS Management", "Seeing, Updating, Adding, Deleting Instructions", "/api/instructions", Set.of("*"));

            Role admin = addRole("ADMIN",Set.of(mapManagement, seeingAction, roleManagement, userManagement, departmentsManagement, cmsManagement));
            Role user = addRole("USER",Set.of(seeingMap, seeingRoles, seeingUsers, seeingDepartments, seeingInstructions));

            Department admins=addDepartment( "Admins");

            addUser("John", "Doe", "admin@gmail.com", passwordEncoder.encode( "qwerty123$"),admin, admins.getId());
            addUser("any", "any", "any@gmail.com", passwordEncoder.encode( "any"),user,null);
        };
    }

    private Action addAction(String name, String description, String allowedPath, Set<String> methods){
        Action action=actionRepo.findByName(name).orElse(new Action());
        action.setName(name);
        action.setDescription(description);
        action.setAllowedPath(allowedPath);
        action.setAllowedMethods(methods);
        return actionRepo.save(action);
    }

    private Role addRole(String name, Set<Action>actions){
        Role role=roleRepo.findByName(name).orElse(new Role());
        role.setName(name);
        role.setActions(actions);
        return roleRepo.save(role);
    }

    private Department addDepartment(String name){
        Department department=departmentRepo.findByName(name).orElse(new Department(name));
        return departmentRepo.save(department);
    }

    private User addUser(String firstName, String lastName, String email, String password, Role role, Long departmentId){
        User user=userRepo.findByLogin(email).orElse(new User());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setLogin(email);
        user.setPassword(password);
        user.setRole(role);
        user.setDepartmentId(departmentId);
        return userRepo.save(user);
    }
}
