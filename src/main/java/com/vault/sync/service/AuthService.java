package com.vault.sync.service;

import com.vault.sync.entity.VaultUser;
import com.vault.sync.repository.UserRepository;
import com.vault.sync.utils.DuplicateUsernameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthService(UserRepository _repository){
        this.repository = _repository;
    }

    public void signupUser(VaultUser vaultUser) throws DuplicateUsernameException {
        VaultUser current = repository.findByUsername(vaultUser.getUsername());

        if(current != null)
            throw new DuplicateUsernameException("VaultUser with username: " + vaultUser.getUsername() + " already exist");

        vaultUser.setPassword(passwordEncoder.encode(vaultUser.getPassword()));
        repository.save(vaultUser);
    }
}
