package app.service;

import app.model.Auditorium;
import app.repository.AuditoriumRepository;
import lombok.RequiredArgsConstructor;
import app.model.Key;
import app.model.KeyRequest;
import app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.repository.KeyRepository;
import app.repository.KeyRequestRepository;
import app.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeyRequestService {

    @Autowired
    private  KeyRequestRepository keyRequestRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  KeyRepository keyRepository;
    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Transactional
    public void requestKey(Long userId, Long AuditoriumId, LocalDateTime dateTimeString) {
        User user = userRepository.findById(String.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User  not found"));
        Auditorium auditorium = auditoriumRepository.findById(String.valueOf(AuditoriumId))
                .orElseThrow(() -> new RuntimeException("Key not found"));
        Key key = keyRepository.findById(String.valueOf(AuditoriumId))
                .orElseThrow(() -> new RuntimeException("Key not found"));

        if (auditorium.getKeys().isEmpty()) {
            throw new RuntimeException("Аудитория занята, попробуйте позже");
        }

        KeyRequest keyRequest = KeyRequest.builder()
                .user(user)
                .auditorium(auditorium)
                .requestedAt(LocalDateTime.now())
                .expectedReturnTime(dateTimeString)
                .build();

        this.keyRequestRepository.saveAndFlush(keyRequest);

    }

    public List<KeyRequest> getAllRequests() {
        return keyRequestRepository.findAll();
    }

    public Optional<KeyRequest> findById(String id){ return  keyRequestRepository.findById(id);}
}
