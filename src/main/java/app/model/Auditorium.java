package app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CollectionId;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "auditoriums")
public class Auditorium {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "id")
    private List<Key> keys; // Список ключей, связанных с аудиторией

    @Column(name = "rented_key")
    private Key rentedKey;
}
