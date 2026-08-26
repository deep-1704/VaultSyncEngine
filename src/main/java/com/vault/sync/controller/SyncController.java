package com.vault.sync.controller;

import com.vault.sync.entity.Credential;
import com.vault.sync.entity.SyncItem;
import com.vault.sync.entity.apientity.DeviceCredential;
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
    public ResponseEntity<Void> syncCredToDevices(@RequestBody List<DeviceCredential> deviceCredentials, Authentication authentication){
        Credential credential = new Credential();
        credential.setOwner(authentication.getName());

        if(credential.getId() == null) credential = credentialService.createCredential(credential);
        else if(!credentialService.existsById(credential.getId())) {
            return ResponseEntity.notFound().build();
        }

        for(DeviceCredential dv : deviceCredentials) dv.credential().setId(credential.getId());
        syncService.syncDevices(deviceCredentials);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<List<SyncItem>> getSyncedCredentials(@PathVariable String deviceId){
        List<SyncItem> credentials = syncService.getSyncedCredentials(deviceId);
        return ResponseEntity.ok(credentials);
    }


}
