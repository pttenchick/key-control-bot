package app.service;

import app.model.Auditorium;
import app.repository.AuditoriumRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriumService {

    @Autowired
    public AuditoriumRepository auditoriumRepository;

    @Transactional
    public List<Auditorium> getAll(){
        return auditoriumRepository.findAll();
    }
}
