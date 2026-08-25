package com.vault.sync.repository;

import com.vault.sync.entity.VaultUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<VaultUser, String> {
    VaultUser findByUsername(String Username);
}
