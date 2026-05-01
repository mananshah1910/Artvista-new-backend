package com.artvista.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ArtCategoryRepository extends JpaRepository<com.artvista.backend.model.ArtCategory, Long> {
    Optional<com.artvista.backend.model.ArtCategory> findByName(String name);
}
