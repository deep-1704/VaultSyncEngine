package com.vault.sync.service;

import com.vault.sync.entity.ShareItem;
import com.vault.sync.entity.SharedCredential;
import com.vault.sync.repository.ShareItemRepository;
import com.vault.sync.repository.SharedCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShareService {
    private final SharedCredentialRepository sharedCredentialRepository;
    private final ShareItemRepository shareItemRepository;

    @Autowired
    public ShareService(
            SharedCredentialRepository sharedCredentialRepository,
            ShareItemRepository shareItemRepository
    ) {
        this.sharedCredentialRepository = sharedCredentialRepository;
        this.shareItemRepository = shareItemRepository;
    }

    public void createShareEntries(List<ShareItem> shareItems){
        shareItemRepository.saveAll(shareItems);
    }
}