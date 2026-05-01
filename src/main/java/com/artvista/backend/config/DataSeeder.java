package com.artvista.backend.config;

import com.artvista.backend.model.Artwork;
import com.artvista.backend.model.ArtCategory;
import com.artvista.backend.model.Exhibition;
import com.artvista.backend.model.User;
import com.artvista.backend.repository.ArtworkRepository;
import com.artvista.backend.repository.ArtCategoryRepository;
import com.artvista.backend.repository.ExhibitionRepository;
import com.artvista.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private ArtCategoryRepository artCategoryRepository;

    @Autowired
    private ExhibitionRepository exhibitionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Categories
        if (artCategoryRepository.count() == 0) {
            artCategoryRepository.save(new ArtCategory("Painting", "Traditional and modern paintings on canvas or paper."));
            artCategoryRepository.save(new ArtCategory("Digital Art", "Art created using digital technology and software."));
            artCategoryRepository.save(new ArtCategory("Sculpture", "Three-dimensional art pieces made from various materials."));
            artCategoryRepository.save(new ArtCategory("Photography", "Capturing moments and concepts through the lens."));
        }

        // 2. Seed Artworks
        if (artworkRepository.count() == 0) {
            ArtCategory painting = artCategoryRepository.findByName("Painting").orElse(null);
            ArtCategory digital = artCategoryRepository.findByName("Digital Art").orElse(null);

            Artwork a1 = new Artwork();
            a1.setTitle("Ethereal Whispers");
            a1.setArtist("Elena Vance");
            a1.setPrice(99000.0);
            a1.setHistory("Inspired by the misty mornings of the Scottish Highlands, this piece explores the thin line between reality and dreams.");
            a1.setMedium("Oil on Canvas");
            a1.setYear("2023");
            a1.setImage("https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?q=80&w=1000");
            a1.setStatus("approved");
            a1.setArtCategory(painting);

            Artwork a2 = new Artwork();
            a2.setTitle("Vibrant Chaos");
            a2.setArtist("Julian Thorne");
            a2.setPrice(70000.0);
            a2.setHistory("A study of urban rhythm and color, capturing the raw energy of modern street life.");
            a2.setMedium("Acrylic and Mixed Media");
            a2.setYear("2024");
            a2.setImage("https://images.unsplash.com/photo-1541963463532-d68292c34b19?q=80&w=1000");
            a2.setStatus("approved");
            a2.setArtCategory(painting);

            Artwork a7 = new Artwork();
            a7.setTitle("Urban Pulse");
            a7.setArtist("Dexter Volt");
            a7.setPrice(58000.0);
            a7.setHistory("The electric energy of the modern metropolis at dusk with neon streaks and blurred figures.");
            a7.setMedium("Digital painting printed on aluminum");
            a7.setYear("2024");
            a7.setImage("https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=1000");
            a7.setStatus("approved");
            a7.setArtCategory(digital);

            artworkRepository.save(a1);
            artworkRepository.save(a2);
            artworkRepository.save(a7);
        }

        // 3. Seed Exhibitions
        if (exhibitionRepository.count() == 0) {
            Exhibition e1 = new Exhibition();
            e1.setTitle("Modern Echoes");
            e1.setDescription("Highlighting contemporary abstract masters and their contribution to the art world.");
            e1.setCurator("Sarah Jenkins");
            exhibitionRepository.save(e1);
        }

        // 4. Seed Users
        if (userRepository.findByEmail("2400090137@kluniversity.in").isEmpty()) {
            User admin = new User();
            admin.setEmail("2400090137@kluniversity.in");
            admin.setPassword(passwordEncoder.encode("Manan@1910"));
            admin.setName("Manan Shah");
            admin.setRole("admin");
            admin.setFirstLogin(false);
            userRepository.save(admin);
        }

        if (userRepository.findByEmail("curator@artvista.art").isEmpty()) {
            User curator = new User();
            curator.setEmail("curator@artvista.art");
            curator.setPassword(passwordEncoder.encode("curator123"));
            curator.setName("Sarah Jenkins");
            curator.setRole("curator");
            curator.setFirstLogin(false);
            userRepository.save(curator);
        }

        if (userRepository.findByEmail("artist@artvista.art").isEmpty()) {
            User artist = new User();
            artist.setEmail("artist@artvista.art");
            artist.setPassword(passwordEncoder.encode("artist123"));
            artist.setName("Elena Vance");
            artist.setRole("artist");
            artist.setFirstLogin(false);
            userRepository.save(artist);
        }
    }
}
