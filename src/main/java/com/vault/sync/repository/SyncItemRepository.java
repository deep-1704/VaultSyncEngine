package com.vault.sync.repository;

import com.vault.sync.entity.SyncItem;
import com.vault.sync.entity.compositekey.SyncItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncItemRepository extends JpaRepository<SyncItem, SyncItemId> {
}
