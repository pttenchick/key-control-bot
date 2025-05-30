package app.utils;

import app.bot.TelegramLongPollingBot;
import app.events.MessageReceivedEvent;
import app.model.*;
import app.repository.AuditoriumRepository;
import app.service.IKeyService;
import app.session.KeyRequestSession;
import app.session.MySession;
import app.session.SessionManager;
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

    String helloForAdmin =
                "Добро пожаловать в систему контроля ключей!\n\n"

                + "Доступные команды:\n"
                + "/start - получить стартовое сообщение с командами\n\n"

                + "Получить список всех ключей\n" //Засунуть в меню
                + "Получить список всех запросов\n" //Засунуть в меню
                + "Получить список всех аудиторий\n\n" //Засунуть в меню

                + "Одобрить запрос [номер]\n"
                + "Вернуть ключ [номер аудитории]\n\n"

                + "Добавить аудиторию\n" //Засунуть в меню
                + "Аудитория [номер аудитории]\n"
                + "Изменить название аудитории\n"
                + "Удалить аудиторию [номер]\n\n"

                + "Добавить ключ\n" //Засунуть в меню
                + "Ключ [номер]\n"
                + "Изменить привязку ключа\n" //Засунуть в меню
                + "Удалить ключ [номер]\n\n"

                + "Получить PDF отчет на почту" //Засунуть в меню
            ;

    private final long adminId = 1022433076;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private TelegramLongPollingBot bot;

    @Autowired
    private KeyRequestService keyRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private IKeyService keyService;

    @Autowired
    private SessionManager sessionManager;

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
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

    public void handle(Message message){
        Long chatId = message.getChatId();

        User user = userService.getUserById(chatId);

        switch (user.getCondition()){
            case NONE -> processData();
            case SEND_MESSAGE_TO_ADMIN -> sendMessageToAdmin();
            case REQUEST_KEY -> createRequestKey();
            case CREATE_KEY -> createKey();
            case CREATE_AUDITORIUM -> createAuditorium();
            case UPDATE_KEY -> updateKey();
            case UPDATE_TIME_REQUEST -> updateReturnTime();
            case UPDATE_ID_AUDITORIUM -> updateIdAuditorium();
            case UPDATE_NAME_AUDITORIUM -> updateNameAuditorium();
            case UPDATE_ID_AUDITORIUM_REQUEST -> updateIdByAuditoriumInRequest();

            default -> sendMessage(chatId, "Сообщение не распознано");
        }
    }


    public void processData(Message message, User user){
        Long chatId = message.getChatId();

        if(user.getRole() == Role.ADMIN){

            switch (message.getText()){
                case "/start":
                    sendMessage(chatId, "");
            }
        }
    }






















//    public void handle(Message message) {
//        String userMessage = message.getText();
//        String chatId = String.valueOf(message.getChatId());
//
//        User user = userService.getUserById(message.getChatId());
//
//        String hello = "Добро пожаловать в систему контроля ключей!\n\n"
//                + "Доступные команды:\n"
//                + "/start - получить стартовое сообщение с командами\n"
//                + "/request_key [номер аудитории] [время возврата ключа в формате HH:mm dd/MM/yyyy] - Запрос на выдачу ключа\n";
//
//        String helloForAdmin = "Добро пожаловать в систему контроля ключей!\n\n"
//                + "Доступные команды:\n"
//                + "/start - получить стартовое сообщение с командами\n"
//                + "/get_all_keys - Получить список всех ключей\n"
//                + "/get_all_request - Получить список всех запросов\n"
//                + "/accept_request [номер запроса] - одобрить выдачу ключа\n"
//                + "/return_key [номер аудитории]\n"
//                + "/request_key [номер аудитории] [время возврата ключа в формате HH:mm dd/MM/yyyy] - Запрос на выдачу ключа\n"
//                + "/create_auditorium [номер аудитории] [название]\n"
//                + "/create_auditorium [номер аудитории] [название] — создать новую аудиторию\n"
//                + "/read_auditorium [номер аудитории]  — получить информацию об аудитории по номеру\n"
//                + "/update_auditorium [номер аудитории] [новое название] — обновить название аудитории по номеру\n"
//                + "/delete_auditorium [номер аудитории]  — удалить аудиторию по номеру\n"
//                + "/list_auditoriums  — получить список всех аудиторий\n"
//                + "/create_key [идентификатор ключа] [название] — создать новый ключ\n"
//                + "/read_key [идентификатор ключа]  — получить информацию о ключе по идентификатору\n"
//                + "/update_key [идентификатор ключа] [новое название] — обновить название ключа по идентификатору\n"
//                + "/delete_key [идентификатор ключа] — удалить ключ по идентификатору\n"
//                + "/list_keys — получить список всех ключей\n";
//
//        String error = "Неизвестная команда, попробуйте заново!\n";
//
//        if (userMessage.startsWith("/start")) {
//
//            if(sessionManager.isHasSession(message.getChatId())){
//                sendMessage(chatId, "У вас есть незавершенная сессия");
//                manageSession(message);
//            }
//
//            if (user.getRole().equals(Role.ADMIN)) {
//                sendMessage(chatId, helloForAdmin);
//            }
//            else {
//                sendMessage(chatId, hello);
//            }
//        } else if (userMessage.startsWith("/request_key")) {
//            String[] words = userMessage.split(" ");
//            long auditoriumId = Long.parseUnsignedLong(words[1]);
//
//                if (auditoriumId < 9999) {
//                    Key key = keyService.getAvailableKeys(auditoriumId);
//                    Auditorium auditorium = auditoriumRepository.getAuditoriumById(auditoriumId);
//                    if(!auditorium.getKeys().isEmpty()){
//                        String time = words[2] + " " + words[3];
//                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
//                        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
//
//                        LocalDateTime now = LocalDateTime.now();
//                        if (dateTime.isAfter(now.minusMinutes(10))) {
//
//                            keyRequestService.createRequestKey(user.getId(), auditoriumId, dateTime);
//                            sendMessage(chatId, "Запрос отправлен администратору");
//                            sendMessage(String.valueOf(adminId), "Пришел новый запрос на выдачу ключа");
//                        }
//                        else {sendMessage(chatId, "Неправильный ввод даты, попробуйте отправить запрос заново");}
//                    }
//                    else {
//                        sendMessage(chatId, "Аудитория занята, попробуйте позже");
//                    }
//                }
//                else {
//                    sendMessage(chatId, "Ключ от данной аудитории не найден");
//                }
//
//        } else if (user.getRole().equals(Role.ADMIN)) {
//            if (userMessage.startsWith("/get_all_keys")) {
//                List<Key> listKeys = keyService.getAllKeys();
//
//                String response = listKeys.stream()
//                        .map(keyService::formatKeyInfo)
//                        .collect(Collectors.joining("\n\n"));
//
//                if (response.isEmpty()) {
//                    response = "Нет доступных ключей";
//                }
//
//                sendMessage(String.valueOf(adminId), response);
//            } else if (userMessage.startsWith("/get_all_request")) {
//                List<KeyRequest> requestList = keyRequestService.getAllRequests();
//                for (KeyRequest request : requestList) {
//                    String data = "Запрос N" + request.getId() + "\nПользователь " + request.getUser ().getUsername() +
//                            " запрашивает ключ от аудитории " + request.getAuditorium().getId()  +
//                            " до " + request.getExpectedReturnTimeInString() + "\n";
//                    sendMessage(String.valueOf(adminId), data);
//                }
//            } else if (userMessage.startsWith("/accept_request")) {
//                String[] words = userMessage.split(" ");
//                if(!words[1].isEmpty() && Integer.parseInt(words[1]) > 0) {
//                    Optional<KeyRequest> keyRequest = keyRequestService.findById(words[1]);
//                    if (keyRequest.isPresent()) {
//                        KeyRequest request = keyRequest.get();
//                        keyService.issueKey(request.getAuditorium().getId(), request.getUser().getId());
//                        sendMessage(String.valueOf(adminId), "Выдача ключа подтверждена");
//                        sendMessage(String.valueOf(request.getUser().getId()), "Выдан ключ");
//                    }
//                }
//                else {sendMessage(chatId, "Неверное значение номера одобряемого запроса");}
//            } else if (userMessage.startsWith("/return_key")) {
//                String[] words = userMessage.split(" ");
//                keyService.returnKey(Long.parseLong(words[1]));
//                sendMessage(String.valueOf(adminId), "Ключ возвращен");
//            }
//        } else {
//            sendMessage(chatId, error);
//        }
//    }
//
//    public void manageSession(Message message){
//        MySession mySession = sessionManager.getSession(message.getChatId());
//        processInput(message.getChatId(), message, mySession);
//    }
//
//    private void processInput(Long chatId, Message message, MySession mySession){
//        if(mySession instanceof KeyRequestSession){
//            KeyRequestSession keyRequestSession = (KeyRequestSession) mySession;
//
//            switch (keyRequestSession.getStep()){
//                case 0:
//                    sendMessage(String.valueOf(chatId), "Редактирование информации о запросе. \nВведите номер аудитории:");
//
//            }
//
//        }
//
//    }

}
