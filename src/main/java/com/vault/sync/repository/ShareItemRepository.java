package com.vault.sync.repository;

import com.vault.sync.entity.ShareItem;
import com.vault.sync.entity.compositekey.ShareItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareItemRepository extends JpaRepository<ShareItem, ShareItemId> {

    @Query("SELECT s.deviceId FROM ShareItem s WHERE s.sharedCredId = :sharedCredId")
    List<String> findDeviceIdsBySharedCredId(Long sharedCredId);

    List<ShareItem> findAllByDeviceId(String deviceId);

    void deleteAllBySharedCredId(Long sharedCredId);
}
