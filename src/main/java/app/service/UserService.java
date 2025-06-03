package app.service;
import app.model.Condition;
import lombok.RequiredArgsConstructor;
import app.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository; // Репозиторий для пользователей

    @Transactional
    public User addUser(User user) {
        return userRepository.save(user); // Сохраняем нового пользователя
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(); // Получаем всех пользователей
    }

    public User getUserById(Long userId) {
        return userRepository.findById(String.valueOf(userId)).orElseThrow(() -> new RuntimeException("User  not found"));
    }

    public void setCondition(Condition condition, Long chatId){
        User user = userRepository.getUserById(chatId);

        user.setCondition(condition);
        userRepository.saveAndFlush(user);
    }
}

