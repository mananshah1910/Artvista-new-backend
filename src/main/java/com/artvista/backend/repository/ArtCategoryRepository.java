package com.artvista.backend.repository;

import com.artvista.backend.model.ArtCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ArtCategoryRepository extends JpaRepository<ArtCategory, Long> {
    Optional<ArtCategory> findByName(String name);
}
