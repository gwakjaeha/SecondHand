package com.example.secondhand.global.config.jwt;

import com.example.secondhand.domain.user.domain.Account;
import com.example.secondhand.domain.user.repository.AccountRepository;
import com.example.secondhand.global.exception.CustomErrorCode;
import com.example.secondhand.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Component("userDetailsService")
@RequiredArgsConstructor
public class JwtUserService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String email) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        if(!optionalAccount.isPresent()){
            throw new CustomException(CustomErrorCode.NOT_FOUND_USER);
        }

        Account account = optionalAccount.get();

        if(account.ACCOUNT_STATUS_STOP.equals(account.getStatus())){
            throw new CustomException(CustomErrorCode.STOP_EMAIL);
        }

        if(account.ACCOUNT_STATUS_WITHDRAW.equals(account.getStatus())){
            throw new CustomException(CustomErrorCode.WITHDRAW_EMAIL);
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if(account.isAdminYn()){
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new User(account.getEmail(), account.getPassword(), grantedAuthorities);
    }
}
