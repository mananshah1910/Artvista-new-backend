package com.artvista.backend.controller;

import com.artvista.backend.model.Artwork;
import com.artvista.backend.repository.ArtworkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/artworks")
public class ArtworkController {

    @Autowired
    private ArtworkRepository artworkRepository;

    @GetMapping
    public List<Artwork> getAllArtworks(@RequestParam(required = false) String status) {
        if (status != null) {
            return artworkRepository.findByStatus(status);
        }
        return artworkRepository.findAll();
    }

    @PostMapping
    public Artwork addArtwork(@RequestBody Artwork artwork) {
        if (artwork.getStatus() == null) artwork.setStatus("pending");
        return artworkRepository.save(artwork);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveArtwork(@PathVariable @org.springframework.lang.NonNull Long id) {
        return artworkRepository.findById(id)
                .map(artwork -> {
                    artwork.setStatus("approved");
                    artworkRepository.save(artwork);
                    return ResponseEntity.ok(artwork);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArtwork(@PathVariable @org.springframework.lang.NonNull Long id) {
        artworkRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        long count = artworkRepository.count();
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "artworksCount", count,
            "database", "H2 (In-Memory)"
        ));
    }
}
