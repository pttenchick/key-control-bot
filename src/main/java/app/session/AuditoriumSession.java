package app.session;

import app.model.Auditorium;
import lombok.Data;

@Data
public class AuditoriumSession extends MySession {

    private Auditorium auditorium;

    public AuditoriumSession(Long chatId){
        super();
        this.auditorium = new Auditorium();
        setChatId(chatId);
    }

}
