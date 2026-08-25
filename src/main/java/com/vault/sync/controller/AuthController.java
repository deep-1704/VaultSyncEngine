package com.vault.sync.controller;

import com.vault.sync.entity.VaultUser;
import com.vault.sync.service.AuthService;
import com.vault.sync.utils.DuplicateUsernameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService authService){
        this.service = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody VaultUser vaultUser){
        try{
            service.signupUser(vaultUser);
        } catch (DuplicateUsernameException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
