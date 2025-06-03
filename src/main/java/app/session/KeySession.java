package app.session;

import app.model.Key;
import lombok.Data;

@Data
public class KeySession extends MySession {

    private Key key;

    public KeySession(Long chatId){
        super();
        this.key = new Key();
        setChatId(chatId);
    }
    public KeySession(Long chatId, int step){
        super();
        this.key = new Key();
        setChatId(chatId);
        setStep(step);
    }
}
