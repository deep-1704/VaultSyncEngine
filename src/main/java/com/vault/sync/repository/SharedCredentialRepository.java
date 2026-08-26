package com.vault.sync.repository;

import com.vault.sync.entity.SharedCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedCredentialRepository extends JpaRepository<SharedCredential, Long> {
}
