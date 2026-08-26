package com.vault.sync.repository;

import com.vault.sync.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, String> {

    List<Device> findAllByOwner(String owner);

    List<Device> findAllByIdIn(List<String> deviceIds);

}
