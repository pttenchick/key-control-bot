package app.session;

import app.model.KeyRequest;
import lombok.Data;

@Data
public class KeyRequestSession extends MySession {

   private KeyRequest keyRequest;

   public KeyRequestSession(KeyRequest keyRequest, Long chatId){
      super();
      this.keyRequest = keyRequest;
      setChatId(chatId);
   }

   public KeyRequestSession(KeyRequest keyRequest, int step){
      super();
      this.keyRequest = keyRequest;
      setChatId(this.keyRequest.getUser().getId());
      setStep(step);
   }

}
