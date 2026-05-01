package com.artvista.backend.repository;

import com.artvista.backend.model.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByStatus(String status);
}
