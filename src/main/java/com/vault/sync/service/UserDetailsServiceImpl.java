package com.vault.sync.service;

import com.vault.sync.entity.VaultUser;
import com.vault.sync.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;

    @Autowired
    public UserDetailsServiceImpl(UserRepository repository){
        this.repo = repository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        VaultUser vaultUser = repo.findByUsername(username);

        if(vaultUser != null){
            return org.springframework.security.core.userdetails.User.builder()
                    .username(vaultUser.getUsername())
                    .password(vaultUser.getPassword())
                    .roles("USER")
                    .build();
        }

        throw new UsernameNotFoundException("Username not found: "+username);
    }
}
