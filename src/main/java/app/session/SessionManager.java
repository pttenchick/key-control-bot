package app.session;

import app.model.KeyRequest;
import app.model.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SessionManager {

   private Map<Long, MySession> session = new HashMap<>();

    public void startSession(Long chatId, MySession mySession) {
        session.put(chatId, mySession);
    }

    public MySession getSession(Long chatId) {
        return session.get(chatId);
    }

    public void endSession(Long chatId) {
        session.remove(chatId);
    }

    public boolean isHasSession(Long chatId){
       return session.containsKey(chatId);
    }



}
