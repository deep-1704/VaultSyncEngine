package com.vault.sync.controller;

import com.vault.sync.entity.ShareItem;
import com.vault.sync.entity.SharedCredential;
import com.vault.sync.service.ShareService;
import com.vault.sync.service.SharedCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/share")
public class ShareController {
    private final ShareService shareService;
    private final SharedCredentialService sharedCredentialService;

    @Autowired
    public ShareController(ShareService shareService, SharedCredentialService sharedCredentialService) {
        this.shareService = shareService;
        this.sharedCredentialService = sharedCredentialService;
    }

    @PostMapping("/{username}")
    public ResponseEntity<SharedCredential> shareCredential(
            @PathVariable String username,
            @RequestBody List<ShareItem> shareItems,
            Authentication authentication
    ){
        String authenticatedUser = authentication.getName();

        // Check if sharing to itself
        if(Objects.equals(authenticatedUser, username)){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

        // Check if sharing other user's credentials
        if(!sharedCredentialService.isOwner(
                shareItems.stream().map(ShareItem::getSharedCredId).toList(),
                authenticatedUser
        )){
            return ResponseEntity.badRequest().build();
        }

        SharedCredential credential = new SharedCredential(authenticatedUser);

        if(shareItems.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        credential.setId(shareItems.getFirst().getSharedCredId());
        if(credential.getId() == null) credential = sharedCredentialService.createSharedCredential(credential);
        else if(!sharedCredentialService.checkSharedCredentialExitsById(credential.getId())){
            return ResponseEntity.notFound().build();
        }

        for(ShareItem si: shareItems) si.setSharedCredId(credential.getId());

        // Add a shareEntry per device
        shareService.createShareEntries(shareItems);
        return ResponseEntity.ok(credential);
    }
}
