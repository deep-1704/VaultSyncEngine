package com.vault.sync.service;

import com.vault.sync.entity.SharedCredential;
import com.vault.sync.repository.SharedCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SharedCredentialService {
    private final SharedCredentialRepository sharedCredentialRepository;

    @Autowired
    public SharedCredentialService(SharedCredentialRepository sharedCredentialRepository) {
        this.sharedCredentialRepository = sharedCredentialRepository;
    }

    public SharedCredential createSharedCredential(SharedCredential sharedCredential){
        return sharedCredentialRepository.save(sharedCredential);
    }

    public boolean checkSharedCredentialExitsById(Long credId){
        return sharedCredentialRepository.existsById(credId);
    }

    public boolean isOwner(List<Long> sharedCredIds, String username){
        List<SharedCredential> sharedCredentials = sharedCredentialRepository.findAllById(sharedCredIds);
        for(SharedCredential sc : sharedCredentials){
            if(!Objects.equals(sc.getOwner(), username)) return false;
        }

        return true;
    }
}
