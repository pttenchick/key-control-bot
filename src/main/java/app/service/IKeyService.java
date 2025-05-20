package app.service;

import app.model.Key;
import java.util.List;

public interface IKeyService {
    List<Key> getAllKeys();
    Key getKeyById(Long keyId);
    boolean checkKeyAvailability(Long keyId);
    Key issueKey(Long keyId, Long userId);
    Key returnKey(Long keyId);
    String formatKeyInfo(Key key);
    Key getAvailableKeys(Long auditoriumId);
}
