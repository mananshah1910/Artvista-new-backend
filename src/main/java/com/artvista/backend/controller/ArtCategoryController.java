package com.artvista.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class ArtCategoryController {

    @Autowired
    private com.artvista.backend.repository.ArtCategoryRepository artCategoryRepository;

    @GetMapping
    public java.util.List<com.artvista.backend.model.ArtCategory> getAllCategories() {
        return artCategoryRepository.findAll();
    }
}
