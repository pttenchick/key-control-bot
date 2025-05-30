package app.session;

import app.model.KeyRequest;
import lombok.Data;

@Data
public class KeyRequestSession extends MySession {

   private KeyRequest keyRequest;

   public KeyRequestSession(){
      super();
      this.keyRequest = new KeyRequest();
      setChatId(keyRequest.getUser().getId());
   }

}
