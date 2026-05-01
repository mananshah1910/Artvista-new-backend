package com.artvista.backend.repository;

import com.artvista.backend.model.UserInquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInquiryRepository extends JpaRepository<UserInquiry, Long> {
}
