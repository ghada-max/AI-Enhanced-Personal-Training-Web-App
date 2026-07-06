package com.fittness.userservice.repo;

import com.fittness.userservice.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Userrepo extends JpaRepository<User,String> {
    boolean existsByEmail(String email);
    boolean existsById(String userId);

    Boolean existsByKeycloakId(String userId);

    User findByEmail(String email);
}
