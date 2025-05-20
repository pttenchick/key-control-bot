package app.repository;

import app.model.Auditorium;
import app.model.Key;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriumRepository extends JpaRepository<Auditorium,String> {

}
