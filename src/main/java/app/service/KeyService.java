package app.service;
import app.model.KeyRequest;
import app.repository.KeyRequestRepository;
import app.utils.MessageHandler;
import app.model.Key;
import app.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.repository.KeyRepository;
import app.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KeyService implements IKeyService{

    private static final Logger logger = LogManager.getLogger(KeyService.class);

    @Autowired
    private KeyRequestRepository requestRepository;
    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Lazy
    private MessageHandler messageHandler;

    @Override
    public List<Key> getAllKeys() {
        logger.info("Сервис запрашивает все ключи");
        return keyRepository.findAll();
    }

    @Override
    public Key getKeyById(Long keyId) {
        logger.info("Сервис запрашивает все ключи");
        return keyRepository.findById(String.valueOf(keyId))
                .orElseThrow(() -> new RuntimeException("Key not found")); // Получаем ключ по ID
    }

    @Override
    public boolean checkKeyAvailability(Long keyId) {
        Key key = getKeyById(keyId);
        logger.info("Сервис проверяет доступность ключа");
        return key.isAvailable(); // Проверяем доступность ключа
    }

    @Override
    @Transactional
    public Key issueKey(Long keyId, Long userId) {
        logger.info("Сервис выдает ключ");
        KeyRequest keyRequest = requestRepository.findByKeyId(keyId);
        Key key = getKeyById(keyId);
        User user = userRepository.findById(String.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User  not found"));

        if (!key.isAvailable()) {
            logger.error("Ключ недоступен");
            throw new RuntimeException("Key is not available"); // Если ключ недоступен, выбрасываем исключение

        }

        key.setAvailable(false);
        key.setUser (user);
        key.setReturnTime(keyRequest.getExpectedReturnTime());
        return keyRepository.saveAndFlush(key); // Сохраняем изменения
    }

    @Override
    @Transactional
    public Key returnKey(Long keyId) {
        logger.info("Сервис возвращает ключ");
       requestRepository.deleteByKeyId(keyId);
        Key key = getKeyById(keyId);
        key.setAvailable(true);
        key.setUser (null);
        key.setReturnTime(null);
        key.setTen(false);
        key.setNow(false);
        key.setLastThirty(false);
        return keyRepository.saveAndFlush(key);
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkTimeKeys(){
        logger.info("Сервис проверяет уведомления");
        LocalDateTime currentTime = LocalDateTime.now();

        List<Key> listNow = keyRepository.findKeysAtReturnTime(currentTime.minusSeconds(25), currentTime.plusSeconds(25));
        if (!listNow.isEmpty()) {
            for (Key key : listNow){
                try {
                    String chatId = String.valueOf(key.getUser().getId());
                    String message = "Внимание: Ключ " + key.getId() + " должен быть возвращен сейчас.";
                    messageHandler.sendMessage(chatId, message);
                }catch (Exception e){
                    logger.error("Ошибка отправки уведомления для ключа {}", key.getId(), e);
                }

            }
        }

        List<Key> listTen = keyRepository.findKeysAtReturnTime(currentTime.plusMinutes(10).minusSeconds(25), currentTime.minusMinutes(10).plusSeconds(25));
        if(!listNow.isEmpty()){
            for (Key key : listTen) {
                try {
                String chatId = String.valueOf(key.getUser().getId());
                String message = "Напоминание: Ключ " + key.getId() + " нужно вернуть через 10 минут.";
                messageHandler.sendMessage(chatId, message);
            } catch (Exception e) {
                // Логируем ошибку, но не прерываем выполнение
                logger.error("Ошибка отправки уведомления для ключа {}", key.getId(), e);
                }
            }
        }

        List<Key> listThirty = keyRepository.findKeysAtReturnTime(currentTime.minusMinutes(30).minusSeconds(25), currentTime.minusMinutes(30).plusSeconds(25));
        if(!listThirty.isEmpty()){
            for (Key key : listThirty) {
                try {
                    String chatId = String.valueOf(key.getUser().getId());
                    String message = "Напоминание: Ключ " + key.getId() + " просрочен на 30 минут.";
                    messageHandler.sendMessage(chatId, message);
                } catch (Exception e) {
                    // Логируем ошибку, но не прерываем выполнение
                    logger.error("Ошибка отправки уведомления для ключа {}", key.getId(), e);
                }
            }
        }
    }



    public String formatKeyInfo(Key key) {
        if (key.isAvailable()){
            return String.format(
                    "Ключ %d: Не выдан",
                    key.getId()
            );
        }
        else {
            return String.format(
                    "Ключ %d: Выдан | Владелец: %s",
                    key.getId(),
                    Optional.ofNullable(key.getUser())
                            .map(User::getUsername)
                            .orElse("Не выдан")
            );
        }
    }
}


