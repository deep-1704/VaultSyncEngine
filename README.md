# Vault Sync Engine

A lightweight, high-performance synchronization backend for end-to-end encrypted (E2EE) password and credential vaults. Built with Spring Boot and PostgreSQL, it manages **multi-device synchronization** and **secure peer-to-peer credential sharing** using device-bound public keys without ever exposing plaintext secrets to the server.

- **Tryout the app:** [APK Link](https://drive.google.com/file/d/1bHbyFUwpU5Y4wKQpGdFewlOO_xTXCgsZ/view?usp=drive_link)

---

### Engineering

| Metric | Detail |
| :--- | :--- |
| **Architecture** | 100% Self-designed |
| **Codebase** | 80% Handcrafted / 20% AI-assisted |

---

## Key Features

- **Zero-Knowledge Architecture**: The server stores only device-encrypted payloads; it has no access to master passwords or plaintext credentials.
- **Multi-Device Synchronization**: Distributes encrypted sync records across all authorized devices belonging to a user account.
- **Secure Asymmetric Sharing**: Shares credentials between distinct users by targeting recipient devices using registered RSA public keys.
- **Stateless HTTP Basic Auth**: Secure, simple authentication for protected endpoints.

---
## Architecture Overview

### Sync Architecture

When synchronizing a credential across a user's registered devices:
1. **Device A** queries the sync server for all registered devices belonging to the authenticated account.
2. For every target device (including **Device B**), **Device A** generates an ephemeral AES-256 key, encrypts the credential JSON with AES-256-GCM, and encrypts the AES key using that target device's RSA public key (RSA-OAEP).
3. The resulting hybrid encrypted packages are uploaded to the sync server.
4. **Device B** requests pending sync payloads, decrypts the ephemeral AES key inside its own TEE using its non-exportable private key, decrypts the ciphertext with AES-256-GCM, and stores the credential locally.

```mermaid
sequenceDiagram
    autonumber
    participant A as Device A (Originating)
    participant TEE_A as Device A Keystore (TEE)
    participant S as Vault Sync Server
    participant B as Device B (Target)
    participant TEE_B as Device B Keystore (TEE)

    Note over A,S: 1. Discover Registered Devices
    A->>S: GET /device (HTTP Basic Auth)
    S-->>A: Return Device List [Device B (PubKey_B), ...]

    Note over A,TEE_A: 2. Decrypt Local Credential & Hybrid Encrypt
    A->>TEE_A: Decrypt local credential using PrivKey_A
    TEE_A-->>A: Plaintext Credential JSON
    rect rgb(20, 30, 45)
        Note over A: Generate ephemeral AES-256 key<br/>Encrypt payload with AES-256-GCM (random 12B IV)<br/>Encrypt AES key with PubKey_B (RSA-OAEP)
    end

    Note over A,S: 3. Upload Per-Device Encrypted Blobs
    A->>S: POST /sync [{ deviceId: "B", content: PackedBlob_B }]
    S-->>A: 200 OK (Sync confirmed, assigned credentialId)

    Note over S,B: 4. Target Device Ingestion
    B->>S: GET /sync/{deviceId_B}
    S-->>B: Return Encrypted Items [{ credentialId, content: PackedBlob_B }]

    Note over B,TEE_B: 5. Hardware Decryption & Local Storage
    B->>B: Unpack [KeyLen | EncKey | IV | Ciphertext]
    B->>TEE_B: Decrypt EncKey using PrivKey_B (RSA-OAEP)
    TEE_B-->>B: Plaintext AES-256 Key
    B->>B: Decrypt Ciphertext using AES-256-GCM
    B->>TEE_B: Re-encrypt plaintext with PubKey_B for local storage
    TEE_B-->>B: Local Ciphertext
    B->>B: Insert / Update in local Room database (isSynced = true)
```

---

### Share Architecture

When sharing a credential peer-to-peer with another user:
1. **Device A (Owner)** queries the sync server for the recipient user's registered devices.
2. **Device A** decrypts the local credential, performs hybrid encryption using the recipient device's public key, and sends the payload to the server.
3. The server validates that the sender is not sharing to themselves and creates or updates a `SharedCredential` record.
4. **Device B (Recipient)** retrieves incoming shared payloads for its device ID, decrypts the payload via its secure hardware (TEE), and saves it marked as a received shared item.
5. If the owner modifies the credential, **Device A** publishes the update to all active recipient devices (`publishSharedUpdate`). If revoked, the server deletes the share records and the recipient's device prunes the item upon synchronization.

```mermaid
sequenceDiagram
    autonumber
    participant A as Device A (Owner)
    participant TEE_A as Device A Keystore (TEE)
    participant S as Vault Sync Server
    participant B as Device B (Recipient)
    participant TEE_B as Device B Keystore (TEE)

    Note over A,S: 1. Lookup Recipient Devices
    A->>S: GET /device?username={recipientUsername}
    S-->>A: Return Recipient Devices [Device B (PubKey_B)]

    Note over A,TEE_A: 2. Decrypt Local & Hybrid Encrypt for Recipient
    A->>TEE_A: Decrypt local credential using PrivKey_A
    TEE_A-->>A: Plaintext Credential JSON
    rect rgb(20, 30, 45)
        Note over A: Generate ephemeral AES-256 key<br/>Encrypt payload with AES-256-GCM<br/>Encrypt AES key with Recipient PubKey_B (RSA-OAEP)
    end

    Note over A,S: 3. Dispatch Shared Credential
    A->>S: POST /share/{recipientUsername} [{ deviceId: "B", sharedCredId: null, content: PackedBlob_B }]
    S-->>A: 200 OK { id: sharedCredId, owner: "ownerUsername" }
    A->>A: Update local entity (isShared = true, serverShareId)

    Note over S,B: 4. Recipient Pulls Shared Items
    B->>S: GET /share/{deviceId_B}
    S-->>B: Return Shared Items [{ sharedCredId, content: PackedBlob_B }]

    Note over B,TEE_B: 5. Recipient Hardware Decryption
    B->>B: Unpack [KeyLen | EncKey | IV | Ciphertext]
    B->>TEE_B: Decrypt EncKey using PrivKey_B (RSA-OAEP)
    TEE_B-->>B: Plaintext AES-256 Key
    B->>B: Decrypt Ciphertext with AES-256-GCM
    B->>TEE_B: Re-encrypt plaintext with PubKey_B for local storage
    TEE_B-->>B: Local Ciphertext
    B->>B: Insert into Room (isShared = true, isReceived = true)

    opt Revocation / Deletion
        A->>S: DELETE /share/{sharedCredId}
        S-->>A: 204 No Content (Deleted globally)
        B->>S: GET /share/{deviceId_B}
        S-->>B: Empty list / 404
        B->>B: Prune revoked shared credential from local Room database
    end
```
---

## Tech Stack

- **Java**: JDK 25 (if building from source)
- **Framework**: Spring Boot 4.1.x, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 14+
- **Containerization**: Docker

---

## Local Setup & Hosting

### Step 1: Database Setup & Schema Initialization

The Vault Sync Engine requires an initialized PostgreSQL database with custom triggers and cascading foreign keys.

#### 1. Spin up an empty PostgreSQL instance (Optional if you already have one)
```bash
docker run -d \
  --name vault-postgres \
  -e POSTGRES_USER=dev \
  -e POSTGRES_PASSWORD=dev \
  -e POSTGRES_DB=dev \
  -p 5432:5432 \
  postgres:16-alpine
```

#### 2. Initialize the Database Schema (Mandatory)
Before starting the server, run `init.sql` from the repository root to create all tables, indexes, and automated orphan cleanup triggers:

```bash
psql "postgres://DB_USER:DB_PASS@host:port/DB_NAME" -f src/main/resources/db/init.sql
```

*Example for the local container started above:*
```bash
psql "postgres://dev:dev@localhost:5432/dev" -f src/main/resources/db/init.sql
```

---

### Step 2: Start the Server

Choose either the pre-built Docker image or run directly from source.

#### Option A: Run via Docker (Recommended)

1. **Pull the Docker image:**
   ```bash
   docker pull deep1704/vault_sync_engine:prod_v2
   ```

2. **Run the container:**
   ```bash
   docker run -d \
     --name vault-sync-engine \
     -p 8080:8080 \
     -e spring_datasource_url="jdbc:postgresql://host.docker.internal:5432/dev" \
     -e spring_datasource_username="dev" \
     -e spring_datasource_password="dev" \
     deep1704/vault_sync_engine:prod_v2
   ```
   > [!TIP]
   > On Linux, add `--add-host=host.docker.internal:host-gateway` to the `docker run` command if connecting to PostgreSQL running on the host machine.

#### Option B: Build and Run from Source

Requires **JDK 25**.

```bash
# Clone and enter the repository
git clone https://github.com/deep-1704/VaultSyncEngine.git
cd VaultSyncEngine

# Run using the Gradle wrapper
./gradlew bootRun
```

Or package into an executable JAR:
```bash
./gradlew bootJar
java -jar build/libs/sync-0.0.1-SNAPSHOT.jar
```

---

### Configuration & Environment Variables

The application can be configured via environment variables or a `.env` file:

| Variable | Description | Default |
| :--- | :--- | :--- |
| `spring_datasource_url` | JDBC connection string | `jdbc:postgresql://localhost:5432/dev` |
| `spring_datasource_username` | PostgreSQL username | `dev` |
| `spring_datasource_password` | PostgreSQL password | `dev` |
| `SERVER_PORT` | HTTP server port | `8080` |

---

## API Reference

### Overview & Security
- **Authentication**: Stateless HTTP Basic Authentication (`httpBasic`) configured via [`SecurityConfig`](src/main/java/com/vault/sync/security/SecurityConfig.java).
- **Public Endpoints**: Only [`POST /auth/signup`](src/main/java/com/vault/sync/controller/AuthController.java) is publicly accessible without credentials.
- **Protected Endpoints**: All other endpoints require HTTP Basic Auth credentials in the `Authorization` header (`Authorization: Basic <base64(username:password)>`).

---

### 1. Authentication Endpoints ([`AuthController`](src/main/java/com/vault/sync/controller/AuthController.java))

#### `POST /auth/signup`
- **Description**: Registers a new user account and registers their initial device under their ownership.
- **Authentication**: None (Public)
- **Request Body**:
  ```json
  {
    "user": {
      "username": "alice",
      "password": "securePassword123"
    },
    "device": {
      "id": "device-uuid-001",
      "public_key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
    }
  }
  ```
- **Responses**:
  - `201 Created` – User and device successfully created.
  - `403 Forbidden` – Device already exists and belongs to another user.
  - `409 Conflict` – Username already exists.

---

#### `POST /auth/login`
- **Description**: Authenticates an existing user and registers a new/active device associated with their account.
- **Authentication**: Required (HTTP Basic Auth)
- **Request Body**:
  ```json
  {
    "id": "device-uuid-002",
    "public_key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
  }
  ```
- **Responses**:
  - `201 Created` – Device successfully registered for the authenticated user.
  - `401 Unauthorized` – Invalid credentials.
  - `403 Forbidden` – Device already exists and belongs to another user.

---

#### `DELETE /auth`
- **Description**: Permanently deletes the authenticated user's own account. All associated data (credentials, devices, shared items) is removed automatically via database-level `ON DELETE CASCADE`.
- **Authentication**: Required (HTTP Basic Auth)
- **Request Body**: None
- **Responses**:
  - `204 No Content` – Account successfully deleted.
  - `401 Unauthorized` – Invalid or missing credentials.

---

### 2. Device Endpoints ([`DeviceController`](src/main/java/com/vault/sync/controller/DeviceController.java))

#### `GET /device`
- **Description**: Fetches all devices belonging to a user. If the `username` query parameter is omitted, it defaults to the authenticated user.
- **Authentication**: Required
- **Query Parameters**:
  - `username` *(optional, string)*: Target username to list devices for.
- **Responses**:
  - `200 OK`:
    ```json
    [
      {
        "id": "device-uuid-001",
        "owner": "alice",
        "public_key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
      }
    ]
    ```

---

#### `GET /device/shared/{sharedCredId}`
- **Description**: Retrieves all devices that have received access to a specific shared credential.
- **Authentication**: Required
- **Path Parameters**:
  - `sharedCredId` *(required, Long)*: The ID of the shared credential.
- **Responses**:
  - `200 OK`:
    ```json
    [
      {
        "id": "device-uuid-003",
        "owner": "bob",
        "public_key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
      }
    ]
    ```

---

#### `DELETE /device/{deviceId}`
- **Description**: Deletes a device belonging to the authenticated user. Related entries in `sync_item` and `share_item` are automatically deleted via database cascading (`ON DELETE CASCADE`).
- **Authentication**: Required
- **Path Parameters**:
  - `deviceId` *(required, string)*: ID of the device to delete.
- **Responses**:
  - `204 No Content` – Device successfully deleted.
  - `403 Forbidden` – Authenticated user is not the owner of the device.
  - `404 Not Found` – Specified device does not exist.

---

### 3. Credential Sharing Endpoints ([`ShareController`](src/main/java/com/vault/sync/controller/ShareController.java))

#### `POST /share/{username}`
- **Description**: Shares credentials with another user's devices. If `sharedCredId` is not provided (`null`), a new [`SharedCredential`](src/main/java/com/vault/sync/entity/SharedCredential.java) is created and owned by the authenticated user. If `sharedCredId` is provided, verifies ownership before adding/updating device share entries.
- **Authentication**: Required
- **Path Parameters**:
  - `username` *(required, string)*: The username of the recipient (cannot be the caller).
- **Request Body**:
  ```json
  [
    {
      "deviceId": "device-uuid-bob-1",
      "sharedCredId": null,
      "content": "encrypted-credential-payload-for-device"
    }
  ]
  ```
- **Responses**:
  - `200 OK`:
    ```json
    {
      "id": 10,
      "owner": "alice"
    }
    ```
  - `400 Bad Request` – Empty list or user is not the owner of the credential.
  - `404 Not Found` – Given `sharedCredId` does not exist.
  - `406 Not Acceptable` – Caller attempted to share credentials with themselves.

---

#### `GET /share/{deviceId}`
- **Description**: Fetches shared credential items (`ShareItem`) destined for the specified device. If `sharedCredId` is provided, returns only the entry corresponding to that credential and device (returning `404 Not Found` if not found). If omitted, returns all shared credential items for the device. Verifies that the device exists and belongs to the authenticated user.
- **Authentication**: Required
- **Path Parameters**:
  - `deviceId` *(required, string)*: Device ID to fetch shared items for.
- **Query Parameters**:
  - `sharedCredId` *(optional, Long)*: If provided, filters to only the item for this shared credential ID.
- **Responses**:
  - `200 OK`:
    ```json
    [
      {
        "deviceId": "device-uuid-bob-1",
        "sharedCredId": 10,
        "content": "encrypted-credential-payload-for-device"
      }
    ]
    ```
  - `403 Forbidden` – Device belongs to another user.
  - `404 Not Found` – Specified device does not exist, or no entry exists for the given `sharedCredId` and `deviceId`.

---

#### `DELETE /share/{sharedCredId}`
- **Description**: Deletes or revokes shared credential access.
  - If called by the **owner**: Deletes all share entries globally.
  - If called by a **recipient**: Revokes access for a specific device (if `deviceId` query param is provided) or all of their own devices.
- **Authentication**: Required
- **Path Parameters**:
  - `sharedCredId` *(required, Long)*: ID of the shared credential to revoke/delete.
- **Query Parameters**:
  - `deviceId` *(optional, string)*: Specific device ID to revoke access from.
- **Responses**:
  - `204 No Content` – Successfully deleted/revoked.
  - `400 Bad Request` – Shared credential does not exist or owner not found.

---

### 4. Credential Synchronization Endpoints ([`SyncController`](src/main/java/com/vault/sync/controller/SyncController.java))

#### `POST /sync`
- **Description**: Syncs encrypted credential items across the authenticated user's devices. If `credentialId` is omitted (`null`), a new [`Credential`](src/main/java/com/vault/sync/entity/Credential.java) record is created. Validates that all target devices belong to the authenticated user.
- **Authentication**: Required
- **Request Body**:
  ```json
  [
    {
      "deviceId": "device-uuid-001",
      "credentialId": null,
      "content": "encrypted-payload-for-device-1"
    },
    {
      "deviceId": "device-uuid-002",
      "credentialId": null,
      "content": "encrypted-payload-for-device-2"
    }
  ]
  ```
- **Responses**:
  - `200 OK`:
    ```json
    {
      "id": 5,
      "owner": "alice"
    }
    ```
  - `400 Bad Request` – Request payload list is empty.
  - `404 Not Found` – Specified `credentialId` does not exist.
  - `406 Not Acceptable` – One or more `deviceId`s do not belong to the authenticated user.

---

#### `GET /sync/{deviceId}`
- **Description**: Fetches all synced credential items destined for the specified device. Verifies that the device belongs to the authenticated user.
- **Authentication**: Required
- **Path Parameters**:
  - `deviceId` *(required, string)*: Device ID to fetch synced items for.
- **Responses**:
  - `200 OK`:
    ```json
    [
      {
        "deviceId": "device-uuid-001",
        "credentialId": 5,
        "content": "encrypted-payload-for-device-1"
      }
    ]
    ```
  - `406 Not Acceptable` – Device does not belong to the authenticated user.

---

#### `DELETE /sync/{credId}`
- **Description**: Deletes a synced credential from a specific device or all devices belonging to the authenticated user. Verifies device and credential ownership before deletion.
- **Authentication**: Required
- **Path Parameters**:
  - `credId` *(required, Long)*: ID of the credential to delete.
- **Query Parameters**:
  - `deviceId` *(optional, string)*: Specific device ID to remove the credential from. If omitted, removes from all user devices.
- **Responses**:
  - `204 No Content` – Successfully deleted.
  - `400 Bad Request` – Device or credential does not belong to the authenticated user.
