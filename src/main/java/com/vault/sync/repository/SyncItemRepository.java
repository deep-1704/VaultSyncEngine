package com.vault.sync.repository;

import com.vault.sync.entity.SyncItem;
import com.vault.sync.entity.compositekey.SyncItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SyncItemRepository extends JpaRepository<SyncItem, SyncItemId> {

    List<SyncItem> findAllByDeviceId(String deviceId);

    @Query("SELECT s.deviceId FROM SyncItem s WHERE s.credentialId = :credentialId")
    List<String> findDeviceIdsByCredentialId(@Param("credentialId") Long credentialId);
}
