package com.example.chorvoqgisbackend.repositories.cms;

import com.example.chorvoqgisbackend.models.cms.Instruction;
import com.example.chorvoqgisbackend.repositories.CustomRepo;

import java.util.List;
import java.util.Optional;

public interface InstructionRepo extends CustomRepo<Instruction, Long> {

    List<Instruction>findAllByParentId(Long parentId);

    Optional<Instruction>findBySlug(String slug);

    Boolean existsBySlug(String slug);

    Boolean existsByParentId(Long parentId);
}
