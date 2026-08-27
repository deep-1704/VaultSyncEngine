package com.vault.sync.controller;

import com.vault.sync.entity.Device;
import com.vault.sync.entity.ShareItem;
import com.vault.sync.entity.SharedCredential;
import com.vault.sync.service.DeviceService;
import com.vault.sync.service.ShareService;
import com.vault.sync.service.SharedCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/share")
public class ShareController {
    private final ShareService shareService;
    private final SharedCredentialService sharedCredentialService;
    private final DeviceService deviceService;

    @Autowired
    public ShareController(
            ShareService shareService,
            SharedCredentialService sharedCredentialService,
            DeviceService deviceService
    ) {
        this.shareService = shareService;
        this.sharedCredentialService = sharedCredentialService;
        this.deviceService = deviceService;
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

    @DeleteMapping("/{sharedCredId}")
    public ResponseEntity<Void> deleteSharedCredential(
            @PathVariable Long sharedCredId,
            @RequestParam(required = false) String deviceId,
            Authentication authentication
    ){
        String authenticatedUser = authentication.getName();
        String sharedCredOwner = sharedCredentialService.getOwnerWithId(sharedCredId);
        if(sharedCredOwner == null){
            return ResponseEntity.badRequest().build();
        }

        if(sharedCredOwner.equals(authenticatedUser)){
            shareService.deleteAllShareEntriesById(sharedCredId);
            return ResponseEntity.noContent().build();
        }

        if(deviceId != null){
            shareService.deleteSharedCredInDevices(
                    sharedCredId,
                    new ArrayList<>(List.of(deviceId))
            );
            return ResponseEntity.noContent().build();
        }

        List<Device> userDevices = deviceService.getDevicesByOwner(authenticatedUser);
        List<String> deviceIds = userDevices.stream().map(Device::getId).toList();

        shareService.deleteSharedCredInDevices(sharedCredId, deviceIds);
        return ResponseEntity.noContent().build();
    }
}
