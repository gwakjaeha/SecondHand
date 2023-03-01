package com.example.secondhand.domain.user.repository;

import com.example.secondhand.domain.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByEmailAuthKey(String uuid);
	boolean existsByEmail(String email);
	Optional<User> findOneByEmail(String email);
}
