package com.vault.sync.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Device {
    @Id
    String id;
    String owner;
    String public_key;
}
