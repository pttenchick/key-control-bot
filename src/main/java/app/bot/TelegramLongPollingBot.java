package app.bot;


import app.events.MessageReceivedEvent;
import app.utils.MessageHandler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


/**
 * The class for used to initializing telegram bot without webhook
 *
 * @author Azimjon Nazarov
 */
@Component
public class TelegramLongPollingBot extends org.telegram.telegrambots.bots.TelegramLongPollingBot {

    private final BotConfig botConfig;


    private final ApplicationEventPublisher eventPublisher;


    public TelegramLongPollingBot(BotConfig botConfig, ApplicationEventPublisher eventPublisher) throws TelegramApiException {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.eventPublisher = eventPublisher;

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(this);
        System.out.println("Бот зарегистрирован");
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            eventPublisher.publishEvent(new MessageReceivedEvent(update.getMessage()));
        }
    }
}