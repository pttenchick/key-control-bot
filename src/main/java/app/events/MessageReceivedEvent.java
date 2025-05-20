package app.events;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Message;

@Getter
public class MessageReceivedEvent {
    private final Message message;

    public MessageReceivedEvent(Message message) {
        this.message = message;
    }

}
