package app.utils;

import app.bot.TelegramLongPollingBot;
import app.events.MessageReceivedEvent;
import app.model.Key;
import app.model.KeyRequest;
import app.model.Role;
import app.model.User;
import app.service.IKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import app.service.KeyRequestService;
import app.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MessageHandler {

    private final long adminId = 1022433076;

    @Autowired
    private TelegramLongPollingBot bot;

    @Autowired
    private KeyRequestService keyRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private IKeyService keyService;

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleMessageReceived(MessageReceivedEvent event) {
        handle(event.getMessage());
    }

    public void handle(Message message) {
        String userMessage = message.getText();
        String chatId = String.valueOf(message.getChatId());

        User user = userService.getUserById(message.getChatId());

        String hello = "Добро пожаловать в систему контроля ключей!\n\n"
                + "Доступные команды:\n"
                + "/start - получить стартовое сообщение с командами\n"
                + "/request_key [номер аудитории] [время возврата ключа в формате HH:mm dd/MM/yyyy] - Запрос на выдачу ключа\n";

        String helloForAdmin = "Добро пожаловать в систему контроля ключей!\n\n"
                + "Доступные команды:\n"
                + "/start - получить стартовое сообщение с командами\n"
                + "/get_all_keys - Получить список всех ключей\n"
                + "/get_all_request - Получить список всех запросов\n"
                + "/accept_request [номер запроса] - одобрить выдачу ключа\n"
                + "/return_key [номер аудитории]\n"
                    + "/request_key [номер аудитории] [время возврата ключа в формате HH:mm dd/MM/yyyy] - Запрос на выдачу ключа\n";

        String error = "Неизвестная команда, попробуйте заново!\n";

        if (userMessage.startsWith("/start")) {
            if (user.getRole().equals(Role.ADMIN)) {
                sendMessage(chatId, helloForAdmin);
            }
            else {
                sendMessage(chatId, hello);
            }
        } else if (userMessage.startsWith("/request_key")) {
            String[] words = userMessage.split(" ");
            long keyId = Long.parseUnsignedLong(words[1]);

                if (keyId < 9999) {
                Key key = keyService.getKeyById(keyId);
                    if(key.isAvailable()){
                        String time = words[2] + " " + words[3];
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);

                        LocalDateTime now = LocalDateTime.now();
                        if (dateTime.isAfter(now.minusMinutes(10))) {

                            keyRequestService.requestKey(user.getId(), keyId, dateTime);
                            sendMessage(chatId, "Запрос отправлен администратору");
                            sendMessage(String.valueOf(adminId), "Пришел новый запрос на выдачу ключа");
                        }
                        else {sendMessage(chatId, "Неправильный ввод даты, попробуйте отправить запрос заново");}
                    }
                    else {
                        sendMessage(chatId, "Ключ занят, попробуйте позже");
                    }

                }
                else {
                    sendMessage(chatId, "Ключ от данной аудитории не найден");
                }

        } else if (user.getRole().equals(Role.ADMIN)) {
            if (userMessage.startsWith("/get_all_keys")) {
                List<Key> listKeys = keyService.getAllKeys();

                String response = listKeys.stream()
                        .map(keyService::formatKeyInfo)
                        .collect(Collectors.joining("\n\n"));

                if (response.isEmpty()) {
                    response = "Нет доступных ключей";
                }

                sendMessage(String.valueOf(adminId), response);
            } else if (userMessage.startsWith("/get_all_request")) {
                List<KeyRequest> requestList = keyRequestService.getAllRequests();
                for (KeyRequest request : requestList) {
                    String data = "Запрос N" + request.getId() + "\nПользователь " + request.getUser ().getUsername() +
                            " запрашивает ключ " + request.getAuditorium().getId() +
                            " до " + request.getExpectedReturnTimeInString() + "\n";
                    sendMessage(String.valueOf(adminId), data);
                }
            } else if (userMessage.startsWith("/accept_request")) {
                String[] words = userMessage.split(" ");
                if(!words[1].isEmpty() && Integer.parseInt(words[1]) > 0) {
                    Optional<KeyRequest> keyRequest = keyRequestService.findById(words[1]);
                    if (keyRequest.isPresent()) {
                        KeyRequest request = keyRequest.get();
                        keyService.issueKey(request.getAuditorium().getId(), request.getUser().getId());
                        sendMessage(String.valueOf(adminId), "Выдача ключа подтверждена");
                        sendMessage(String.valueOf(request.getUser().getId()), "Выдан ключ");
                    }
                }
                else {sendMessage(chatId, "Неверное значение номера одобряемого запроса");}
            } else if (userMessage.startsWith("/return_key")) {
                String[] words = userMessage.split(" ");
                keyService.returnKey(Long.parseLong(words[1]));
                sendMessage(String.valueOf(adminId), "Ключ возвращен");
            }
        } else {
            sendMessage(chatId, error);
        }
    }

    public void sendReminde(String chatId, String text) {
        sendMessage(chatId, text);
    }
}
