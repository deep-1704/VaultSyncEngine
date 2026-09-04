-- FK Constraints with ON DELETE CASCADE

ALTER TABLE sync_item
    DROP CONSTRAINT IF EXISTS fk_sync_item_credential;
ALTER TABLE sync_item
    ADD CONSTRAINT fk_sync_item_credential
    FOREIGN KEY (credential_id)
    REFERENCES credential(id)
    ON DELETE CASCADE;

ALTER TABLE sync_item
    DROP CONSTRAINT IF EXISTS fk_sync_item_device;
ALTER TABLE sync_item
    ADD CONSTRAINT fk_sync_item_device
    FOREIGN KEY (device_id)
    REFERENCES device(id)
    ON DELETE CASCADE;

ALTER TABLE share_item
    DROP CONSTRAINT IF EXISTS fk_share_item_shared_credential;
ALTER TABLE share_item
    ADD CONSTRAINT fk_share_item_shared_credential
    FOREIGN KEY (shared_cred_id)
    REFERENCES shared_credential(id)
    ON DELETE CASCADE;

ALTER TABLE share_item
    DROP CONSTRAINT IF EXISTS fk_share_item_device;
ALTER TABLE share_item
    ADD CONSTRAINT fk_share_item_device
    FOREIGN KEY (device_id)
    REFERENCES device(id)
    ON DELETE CASCADE;

-- Trigger — sync_item → credential

CREATE OR REPLACE FUNCTION cleanup_credential_if_orphaned()
RETURNS TRIGGER AS $$
DECLARE
    remaining_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO remaining_count
    FROM sync_item
    WHERE credential_id = OLD.credential_id;

    IF remaining_count = 0 THEN
        DELETE FROM credential WHERE id = OLD.credential_id;
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_cleanup_credential_after_sync_item_delete ON sync_item;

CREATE TRIGGER trg_cleanup_credential_after_sync_item_delete
    AFTER DELETE ON sync_item
    FOR EACH ROW
    EXECUTE FUNCTION cleanup_credential_if_orphaned();


-- Trigger — share_item → shared_credential

CREATE OR REPLACE FUNCTION cleanup_shared_credential_if_orphaned()
RETURNS TRIGGER AS $$
DECLARE
    remaining_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO remaining_count
    FROM share_item
    WHERE shared_cred_id = OLD.shared_cred_id;

    IF remaining_count = 0 THEN
        DELETE FROM shared_credential WHERE id = OLD.shared_cred_id;
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_cleanup_shared_credential_after_share_item_delete ON share_item;

CREATE TRIGGER trg_cleanup_shared_credential_after_share_item_delete
    AFTER DELETE ON share_item
    FOR EACH ROW
    EXECUTE FUNCTION cleanup_shared_credential_if_orphaned();


-- One-time Cleanup

DELETE FROM credential
WHERE id NOT IN (SELECT DISTINCT credential_id FROM sync_item);

DELETE FROM shared_credential
WHERE id NOT IN (SELECT DISTINCT shared_cred_id FROM share_item);
