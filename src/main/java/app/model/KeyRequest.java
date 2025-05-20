package app.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "key_request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "auditorium_id") // Изменено на auditorium_id
    private Auditorium auditorium; // Связь с аудиторией

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "expected_return_time")
    private LocalDateTime expectedReturnTime;

    public String getExpectedReturnTimeInString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        return this.expectedReturnTime.format(formatter);
    }


}
