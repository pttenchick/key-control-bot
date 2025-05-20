package app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Entity
@Table(name = "keys")
@NoArgsConstructor
@AllArgsConstructor
public class Key {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium; // Связь с аудиторией

    @Column(nullable = false, name = "is_available")
    private boolean isAvailable; // Доступность ключа

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Пользователь, который взял ключ

    @Column(name = "return_time")
    private LocalDateTime returnTime; // Время возврата ключа

    @Column(name = "ten")
    private Boolean ten; // Дополнительные параметры

    @Column(name = "now")
    private Boolean now;

    @Column(name = "last_thirty")
    private Boolean lastThirty;

    // Метод для проверки доступности ключа
    public boolean isAvailable() {
        return this.isAvailable;
    }
}
