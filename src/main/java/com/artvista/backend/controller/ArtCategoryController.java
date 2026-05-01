package com.artvista.backend.controller;

import com.artvista.backend.model.ArtCategory;
import com.artvista.backend.repository.ArtCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class ArtCategoryController {

    @Autowired
    private ArtCategoryRepository artCategoryRepository;

    @GetMapping
    public List<ArtCategory> getAllCategories() {
        return artCategoryRepository.findAll();
    }
}
