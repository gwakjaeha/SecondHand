package com.example.secondhand.domain.user.repository;

import com.example.secondhand.domain.user.domain.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByEmail(String email);
	Optional<Account> findByEmailAuthKey(String uuid);
	boolean existsByEmail(String email);
	Optional<Account> findOneByEmail(String email);
}
