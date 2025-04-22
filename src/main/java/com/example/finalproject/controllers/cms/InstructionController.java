package com.example.chorvoqgisbackend.controllers.cms;

import com.example.chorvoqgisbackend.models.cms.Instruction;
import com.example.chorvoqgisbackend.service.cms.InstructionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/instructions")
@CrossOrigin()
public class InstructionController {

    @Autowired
    private InstructionService instructionService;

    @GetMapping
    ResponseEntity<List<Instruction>> getInstructions(
            @RequestParam(required = false)String query, @RequestParam(required = false)Long parentId
    ){

        return ResponseEntity.ok(instructionService.getInstructions(query, parentId).stream().peek(Instruction::hide).toList());
    }

    @GetMapping("/root")
    ResponseEntity<List<Instruction>>getRootInstructions(@RequestParam(required = false)String query){

        return ResponseEntity.ok(instructionService.getRootInstructions(query).stream().peek(Instruction::hide).toList());
    }


    @GetMapping("/{id}")
    ResponseEntity<Instruction>getInstruction(@PathVariable Long id){

        Instruction instruction=instructionService.getInstructionById(id);
        instruction.hide();
        return ResponseEntity.ok(instruction);
    }

    @GetMapping("/byName/{slug}")
    ResponseEntity<Instruction>getInstructionByName(@PathVariable String slug){

        Instruction instruction=instructionService.getInstructionBySlug(slug);
        instruction.hide();
        return ResponseEntity.ok(instruction);
    }


    @PostMapping
    ResponseEntity<Void>addInstruction(@RequestBody Instruction instruction) throws URISyntaxException {

        Instruction newInstruction=instructionService.newInstruction(instruction);
        return ResponseEntity.created(new URI("/api/instructions/"+newInstruction.getSlug())).build();
    }


    @PutMapping("/{id}")
    ResponseEntity<Instruction>updateInstruction(@PathVariable Long id, @RequestBody Instruction instruction){

        Instruction updatedInstruction=instructionService.updateInstruction(id, instruction);
        updatedInstruction.hide();
        return ResponseEntity.ok(updatedInstruction);
    }


    @DeleteMapping("/{id}")
    ResponseEntity<Void>deleteInstruction(@PathVariable Long id){

        instructionService.deleteInstruction(id);
        return ResponseEntity.noContent().build();
    }

}
