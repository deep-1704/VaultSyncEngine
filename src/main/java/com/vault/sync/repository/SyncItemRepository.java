package com.vault.sync.repository;

import com.vault.sync.entity.SyncItem;
import com.vault.sync.entity.compositekey.SyncItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncItemRepository extends JpaRepository<SyncItem, SyncItemId> {

    List<SyncItem> findAllByDeviceId(String deviceId);
}
