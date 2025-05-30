package app.session;

import app.model.KeyRequest;
import app.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionManager {

   private Map<Long, MySession> session = new HashMap<>();

    public void startSession(Long chatId) {
        session.put(chatId, new KeyRequestSession());
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
