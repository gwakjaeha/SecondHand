package com.example.secondhand.global.config.jwt;

import com.example.secondhand.domain.user.domain.User;
import com.example.secondhand.domain.user.repository.UserRepository;
import com.example.secondhand.global.exception.CustomErrorCode;
import com.example.secondhand.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Component("userDetailsService")
@RequiredArgsConstructor
public class JwtUserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String email) {
        Optional<User> optionalAccount = userRepository.findByEmail(email);
        if(!optionalAccount.isPresent()){
            throw new CustomException(CustomErrorCode.NOT_FOUND_USER);
        }

        User user = optionalAccount.get();

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if(user.isAdmin()){
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), grantedAuthorities);
    }
}
