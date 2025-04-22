package com.example.finalproject.service.user;

import com.example.finalproject.config.db.SoftDeleteAspect;
import com.example.finalproject.models.dto.Batch;
import com.example.finalproject.models.dto.user.UserDto;
import com.example.finalproject.models.user.Role;
import com.example.finalproject.models.user.User;
import com.example.finalproject.repositories.user.UserRepo;
import com.example.finalproject.utils.Validator;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleService roleService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SoftDeleteAspect softDeleteAspect;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Batch<User>getUsers(Integer page, Integer pageSize, String name, Long roleId, Long departmentId){

        Pageable pageable=userRepo.getPageable(page,pageSize);
        enableFilter(name, roleId, departmentId);

        Batch<User>userBatch=new Batch<>();

        Page<User> users=userRepo.findAll(pageable);
        userBatch.setData(users.stream().toList());
        userBatch.setCount(users.getTotalElements());

        disableFilter();
        return userBatch;
    }

    public User getUser(Long id){

        Optional<User> userOptional=userRepo.findById(id);
        if(userOptional.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found");
        User user=userOptional.get();
        return user;
    }

    public User newUser(UserDto userDto){

        if(userDto.getEmail()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be empty");
        if(userDto.getRoleId()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role cannot be empty");
        if(userDto.getPassword()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be empty");

        if(!Validator.isValidEmail(userDto.getEmail()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email");
        if(!isLoginValid(userDto.getEmail()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with such email exists");
        Role role=roleService.getRole(userDto.getRoleId());

        User user = User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .login(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(role)
                .build();

        if(userDto.getDepartmentId()!=null)user.setDepartmentId(userDto.getDepartmentId());

        user=userRepo.save(user);

        return user;
    }

    public User updateUser(Long id, UserDto userDto){

        User oldUser=getUser(id);

        if(userDto.getFirstName()!=null)oldUser.setFirstName(userDto.getFirstName());

        if(userDto.getLastName()!=null)oldUser.setLastName(userDto.getLastName());

        if(userDto.getEmail()!=null && isLoginValid(userDto.getEmail()) && Validator.isValidEmail(userDto.getEmail()))oldUser.setLogin(userDto.getEmail());

        if(userDto.getPassword()!=null)oldUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

        if(userDto.getRoleId()!=null){
            Role role=roleService.getRole(userDto.getRoleId());
            oldUser.setRole(role);
        }
        if(userDto.getDepartmentId()!=null)oldUser.setDepartmentId(userDto.getDepartmentId());

        oldUser=userRepo.save(oldUser);

        return oldUser;
    }

    public void deleteUser(Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user=getUser(id);

        if(user.getLogin().equals(authentication.getName()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete yourself");

        userRepo.deleteById(id);
    }

    public Boolean isLoginValid(String name){

        if(name==null)return false;
        softDeleteAspect.disableSoftDeleteFilter();
        boolean ret=userRepo.existsByLogin(name);
        softDeleteAspect.modifyEntityBeforeMethod();
        return !ret;
    }

    private void enableFilter(String name, Long roleId, Long departmentId){
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("userFilter")
                .setParameter("name", name)
                .setParameter("roleId", roleId)
                .setParameter("departmentId", departmentId);
    }

    private void disableFilter(){
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("userFilter");
    }
}
