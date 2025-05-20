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

    @Column(nullable = false)
    private boolean isAvailable;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "ten")
    private Boolean ten;

    @Column(name = "now")
    private Boolean now;

    @Column(name = "last_thirty")
    private Boolean lastThirty;
}

