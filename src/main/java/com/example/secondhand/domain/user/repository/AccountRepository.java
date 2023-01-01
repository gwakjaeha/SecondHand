package com.example.secondhand.domain.user.repository;

import com.example.secondhand.domain.user.domain.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByEmail(String email);

	boolean existsByEmail(String email);
}
