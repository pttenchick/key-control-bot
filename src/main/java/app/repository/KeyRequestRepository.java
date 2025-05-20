package app.repository;

import app.model.KeyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyRequestRepository extends JpaRepository<KeyRequest, String> {
    KeyRequest findByKeyId(Long keyId);
    void deleteByKeyId(Long keyId);
}
