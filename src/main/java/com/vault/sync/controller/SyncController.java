package com.vault.sync.controller;

import com.vault.sync.entity.Credential;
import com.vault.sync.entity.SyncItem;
import com.vault.sync.service.CredentialService;
import com.vault.sync.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sync")
public class SyncController {
    private final SyncService syncService;
    private final CredentialService credentialService;

    @Autowired
    public SyncController(
            SyncService _syncService,
            CredentialService _credentialService
    ){
        this.syncService = _syncService;
        this.credentialService = _credentialService;
    }

    @PostMapping("")
    public ResponseEntity<Credential> syncCredToDevices(
            @RequestBody List<SyncItem> syncItems,
            Authentication authentication
    ){
        if(!checkSyncDevices(syncItems, authentication.getName())){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

        Credential credential = new Credential();
        credential.setOwner(authentication.getName());

        if(syncItems.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        credential.setId(syncItems.getFirst().getCredentialId());

        if(credential.getId() == null) credential = credentialService.createCredential(credential);
        else if(!credentialService.existsById(credential.getId())) {
            return ResponseEntity.notFound().build();
        }

        for(SyncItem si : syncItems) si.setCredentialId(credential.getId());
        syncService.syncDevices(syncItems);

        return ResponseEntity.ok(credential);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<List<SyncItem>> getSyncedCredentials(
            @PathVariable String deviceId,
            Authentication authentication
    ){
        List<SyncItem> credentials = syncService.getSyncedCredentials(deviceId);
        if(!checkSyncDevices(credentials, authentication.getName())){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

        return ResponseEntity.ok(credentials);
    }

    private boolean checkSyncDevices(List<SyncItem> syncItems, String username){
        List<String> deviceIds = syncItems
                .stream()
                .map(SyncItem::getDeviceId)
                .toList();

        return syncService.checkDeviceBelongsToUser(deviceIds, username);
    }

}
