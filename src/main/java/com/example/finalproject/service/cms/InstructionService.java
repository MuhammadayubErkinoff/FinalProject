package com.example.chorvoqgisbackend.service.cms;


import com.example.chorvoqgisbackend.config.db.SoftDeleteAspect;
import com.example.chorvoqgisbackend.models.cms.Instruction;
import com.example.chorvoqgisbackend.repositories.cms.InstructionRepo;
import com.example.chorvoqgisbackend.utils.Validator;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class InstructionService {

    @Autowired
    private InstructionRepo instructionRepo;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SoftDeleteAspect softDeleteAspect;

    public List<Instruction>getInstructions(String query, Long parentId){

        enableFilter(query, parentId);
        List<Instruction> instructions=instructionRepo.findAll();
        disableFilter();
        return instructions;
    }

    public List<Instruction>getRootInstructions(String query){

        enableFilter(query, null);
        List<Instruction> instructions=instructionRepo.findAllByParentId(null);
        disableFilter();
        return instructions;
    }

    public Instruction getInstructionById(Long id){

        Optional<Instruction> instructionOptional=instructionRepo.findById(id);
        if(instructionOptional.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Instruction not found");
        return instructionOptional.get();
    }

    public Instruction getInstructionBySlug(String slug){

        Optional<Instruction> instructionOptional=instructionRepo.findBySlug(slug);
        if(instructionOptional.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Instruction not found");
        return instructionOptional.get();
    }

    public Instruction newInstruction(Instruction instruction){

        instruction.clear();
        if(instruction.getSlug()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instruction should have a slug");
        if(isSlugExists(instruction.getSlug()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instruction with such slug already exists");
        if(!Validator.isValidSlug(instruction.getSlug()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug should only contain digits, letters, space, '-', or '_' characters");

        if(instruction.getParentId()!=null){

            Instruction parent=getInstructionById(instruction.getParentId());
            if(parent.getParentId()!=null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sub instruction cannot be a parent");
        }

        instruction.setSlug(instruction.getSlug().toLowerCase().replace(" ", "_").replace("-","_"));

        return instructionRepo.save(instruction);
    }

    public Instruction updateInstruction(Long id, Instruction instruction){

        Instruction oldInstruction=getInstructionById(id);
        if(instruction.getTitleUz()!=null)oldInstruction.setTitleUz(instruction.getTitleUz());
        if(instruction.getTitleRu()!=null)oldInstruction.setTitleRu(instruction.getTitleRu());
        if(instruction.getTitleEng()!=null)oldInstruction.setTitleEng(instruction.getTitleEng());
        if(instruction.getContentUz()!=null)oldInstruction.setContentUz(instruction.getContentUz());
        if(instruction.getContentRu()!=null)oldInstruction.setContentRu(instruction.getContentRu());
        if(instruction.getContentEng()!=null)oldInstruction.setContentEng(instruction.getContentEng());

        if(instruction.getParentId()!=null){

            if(instructionRepo.existsByParentId(id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instruction is a root instruction, it cannot have a parent");

            Instruction parent=getInstructionById(instruction.getParentId());
            if(Objects.equals(instruction.getParentId(), id)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instruction cannot be parent of itself");
            if(parent.getParentId()!=null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sub instruction cannot be a parent");
            oldInstruction.setParentId(instruction.getParentId());
        }

        return instructionRepo.save(oldInstruction);
    }

    public void deleteInstruction(Long id){

        if(instructionRepo.existsByParentId(id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instruction has child instructions");
        instructionRepo.delete(getInstructionById(id));
    }

    private Boolean isSlugExists(String slug){

        if(slug==null)return false;
        softDeleteAspect.disableSoftDeleteFilter();
        boolean ret = instructionRepo.existsBySlug(slug);
        softDeleteAspect.modifyEntityBeforeMethod();
        return ret;
    }


    private void enableFilter(String query, Long parentId){

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("instructionFilter")
                .setParameter("query", query)
                .setParameter("parentId", parentId);
    }

    private void disableFilter(){

        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("instructionFilter");
    }
}
