package app.repository;

import app.model.Key;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface KeyRepository extends JpaRepository <Key,String> {
    @Query("SELECT k FROM Key k WHERE k.returnTime BETWEEN :now AND :tenMinutesLater")
    List<Key> findKeysToReturn(@Param("now") LocalDateTime now, @Param("tenMinutesLater") LocalDateTime tenMinutesLater);

    @Query("SELECT k FROM Key k WHERE k.returnTime BETWEEN :windowStart AND :windowEnd")
    List<Key> findKeysAtReturnTime(
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );

    @Query("SELECT k FROM Key k WHERE k.returnTime < :thirtyMinutesAgo")
    List<Key> findLateKeys(@Param("thirtyMinutesAgo") LocalDateTime thirtyMinutesAgo);

}
