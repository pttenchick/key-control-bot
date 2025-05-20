package app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "auditoriums")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditorium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Название аудитории

    @OneToMany(mappedBy = "auditorium", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Key> keys; // Список ключей, связанных с аудиторией

    // Метод для получения доступных ключей из аудитории
    public List<Key> getAvailableKeys() {
        return keys.stream()
                .filter(Key::isAvailable) // Фильтруем доступные ключи
                .toList();
    }
}
