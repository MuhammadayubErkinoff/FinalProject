package com.example.finalproject.controllers.user;

import com.example.finalproject.models.dto.Batch;
import com.example.finalproject.models.user.Action;
import com.example.finalproject.service.user.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/actions")
@CrossOrigin()
public class ActionController {

    @Autowired
    private ActionService actionService;

    @GetMapping
    public ResponseEntity<Batch<Action>> getActions(
            @RequestParam(required = false)Integer page, @RequestParam(required = false)Integer pageSize,
            @RequestParam(required = false)String name, @RequestParam(required = false)String allowedPath,
            @RequestParam(required = false) Set<Long> ids
    ){

        Batch<Action>actionBatch;

        if(ids!=null)actionBatch=(actionService.getActions(ids,name,allowedPath));
        else if(page!=null && pageSize!=null)actionBatch=(actionService.getActions(page, pageSize, name, allowedPath));
        else actionBatch=(actionService.getActions(name, allowedPath));

        actionBatch.setData(actionBatch.getData().stream().peek(Action::hide).toList());

        return ResponseEntity.ok(actionBatch);
    }

    @GetMapping("/{id}")
    ResponseEntity<Action>getActionById(@PathVariable("id") Long id){

        Action action= actionService.getAction(id);
        return ResponseEntity.ok(action);
    }


//    @PutMapping("/{id}")
//    ResponseEntity<Action>updateAction(@PathVariable("id") Long id, @RequestBody Action action){
//
//        return ResponseEntity.ok(actionService.updateAction(id, action));
//    }
}
