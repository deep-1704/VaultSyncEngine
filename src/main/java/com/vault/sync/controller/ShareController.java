package com.vault.sync.controller;

import com.vault.sync.entity.ShareItem;
import com.vault.sync.entity.SharedCredential;
import com.vault.sync.service.ShareService;
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

    @Autowired
    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/{username}")
    public ResponseEntity<SharedCredential> shareCredential(
            @PathVariable String username,
            @RequestBody List<ShareItem> shareItems,
            Authentication authentication
    ){
        String authenticatedUser = authentication.getName();
        if(Objects.equals(authenticatedUser, username)){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

        // Create a new Shared Credential
        SharedCredential credential = shareService.createSharedCredential(
                new SharedCredential(authenticatedUser)
        );

        for(ShareItem si: shareItems) si.setSharedCredId(credential.getId());

        // Add a shareEntry per device
        shareService.createShareEntries(shareItems);
        return ResponseEntity.ok(credential);
    }
}
