package app.utils;

import app.bot.TelegramLongPollingBot;
import app.events.MessageReceivedEvent;
import app.model.*;
import app.repository.AuditoriumRepository;
import app.repository.KeyRepository;
import app.repository.KeyRequestRepository;
import app.service.AuditoriumService;
import app.service.IKeyService;
import app.session.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import app.service.KeyRequestService;
import app.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static app.model.Condition.*;

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
                    + "Изменить время возврата\n" //Засунуть в меню
                    + "Удалить ключ [номер]\n\n"

                    + "Получить PDF отчет" //Засунуть в меню
            ;

    String helloForUser =
            "Добро пожаловать в систему контроля ключей!\n\n"

                    + "Доступные команды:\n"
                    + "/start - получить стартовое сообщение с командами\n\n"

                    + "Запрос ключа \n" //Засунуть в меню
                    + "Отправить сообщение администратору \n"; //Засунуть в меню

    private final long adminId = 1022433076;

    @Autowired
    private AuditoriumService auditoriumService;

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

    @Autowired
    private KeyRepository keyRepository;

    @Autowired
    private KeyRequestRepository keyRequestRepository;

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

    public void handle(Message message) {
        Long chatId = message.getChatId();

        User user = userService.getUserById(chatId);

        switch (user.getCondition()) {
            case NONE -> processData(message, user);
            case SEND_MESSAGE_TO_ADMIN -> sendMessageToAdmin(message);
            case REQUEST_KEY -> createRequestKey(message);
            case CREATE_KEY -> createKey(message);
            case CREATE_AUDITORIUM -> createAuditorium(message);
            case UPDATE_KEY -> updateKey(message);
            case UPDATE_TIME_REQUEST -> updateReturnTime(message);
          //  case UPDATE_ID_AUDITORIUM -> updateIdAuditorium();
            case UPDATE_NAME_AUDITORIUM -> updateNameAuditorium(message);
          //  case UPDATE_ID_AUDITORIUM_REQUEST -> updateIdByAuditoriumInRequest();

            default -> sendMessage(chatId, "Сообщение не распознано");
        }
    }


    public void processData(Message message, User user) {
        Long chatId = message.getChatId();

        if (user.getRole() == Role.ADMIN) {

            switch (message.getText()) {
                case "/start":
                    sendMessage(chatId, helloForAdmin);
                    break;

                case "Получить список всех ключей":
                    List<Key> keyList = keyService.getAllKeys();
                    for (Key key : keyList) {
                        sendMessage(chatId, keyService.formatKeyInfo(key));
                    }
                    break;

                case "Получить список всех запросов":
                    List<KeyRequest> requestList = keyRequestService.getAllRequests();
                    for (KeyRequest request : requestList) {
                        String data = "Запрос N" + request.getId() + "\nПользователь " + request.getUser().getUsername() +
                                " запрашивает ключ от аудитории " + request.getAuditorium().getId() +
                                " до " + request.getExpectedReturnTimeInString() + "\n";
                        sendMessage(chatId, data);
                    }
                    break;

                case "Получить список всех аудиторий":
                    List<Auditorium> auditoriumList = auditoriumService.getAll();
                    for (Auditorium auditorium : auditoriumList) {
                        String data = "Аудитория N" + auditorium.getId() + " " + auditorium.getName() + "\n Привязанные ключи: ";
                        List<Key> auditoriumKeys = auditorium.getKeys();
                        for (Key key : auditoriumKeys) {
                            sendMessage(chatId, keyService.formatKeyInfo(key));
                        }
                    }
                    break;

                case "Добавить аудиторию":
                    sessionManager.startSession(chatId, new AuditoriumSession(message.getChatId()));
                    userService.setCondition(CREATE_AUDITORIUM, chatId);
                    sendMessage(chatId, "Введите номер аудитории");
                    break;

                case "Изменить название аудитории":
                    userService.setCondition(UPDATE_NAME_AUDITORIUM, chatId);
                    sendMessage(chatId, "Введите номер и новое название аудитории: [номер], [название]");
                    break;

                case "Добавить ключ":
                    sessionManager.startSession(chatId, new KeySession(message.getChatId()));
                    userService.setCondition(CREATE_KEY, chatId);
                    sendMessage(chatId, "Введите номер аудитории: ");
                    break;

                case "Изменить привязку ключа":
                    userService.setCondition(UPDATE_KEY, chatId);
                    sessionManager.startSession(chatId, new KeySession(chatId, 2));

                    sendMessage(chatId, "Введите номер аудитории: ");
                    break;

//                case "Получить PDF отчет":
//                    wdasdasd;

                case "Запрос ключа":
                    sessionManager.startSession(chatId, new KeyRequestSession(new KeyRequest()));
                    userService.setCondition(REQUEST_KEY, chatId);
                    sendMessage(chatId, "Введите номер аудитории: ");
                    break;

                case "Отправить сообщение администратору":
                    userService.setCondition(SEND_MESSAGE_TO_ADMIN, chatId);
                    sendMessage(chatId, "Введите сообщение: ");
                    break;

                case "Изменить время возврата":
                    userService.setCondition(UPDATE_TIME_REQUEST, chatId);
                    sessionManager.startSession(chatId, new KeyRequestSession(new KeyRequest(), 2));
                    sendMessage(chatId, "Введите номер заявки: ");
                    break;


                default:
                    if (message.getText().contains("Одобрить запрос")) {
                        String[] words = message.getText().split(" ");

                        if (!words[2].isEmpty() && Integer.parseInt(words[2]) > 0) {

                            Optional<KeyRequest> keyRequest = keyRequestService.findById(words[2]);

                            if (keyRequest.isPresent()) {
                                KeyRequest request = keyRequest.get();
                                keyService.issueKey(request.getAuditorium().getId(), request.getUser().getId());
                                sendMessage(adminId, "Выдача ключа подтверждена");
                                sendMessage(request.getUser().getId(), "Выдан ключ");
                            }
                        } else {
                            sendMessage(chatId, "Неверное значение номера одобряемого запроса");
                        }
                    } else if (message.getText().contains("Вернуть ключ")) {
                        String[] words = message.getText().split(" ");
                        keyService.returnKey(Long.parseLong(words[2]));
                        sendMessage(adminId, "Ключ возвращен");

                    } else if (message.getText().startsWith("Аудитория")) {

                        String[] words = message.getText().split(" ");
                        Auditorium auditorium = auditoriumService.auditoriumRepository.getAuditoriumById(Long.valueOf(words[1]));

                        String data = "Аудитория N" + auditorium.getId() + " " + auditorium.getName() + "\n Привязанные ключи: ";
                        List<Key> auditoriumKeys = auditorium.getKeys();
                        for (Key key : auditoriumKeys) {
                            sendMessage(chatId, keyService.formatKeyInfo(key));
                        }

                    } else if (message.getText().contains("Удалить аудиторию")) {
                        String[] words = message.getText().split(" ");
                        auditoriumService.auditoriumRepository.deleteById(words[2]);
                    } else if (message.getText().startsWith("Ключ")) {
                        String[] words = message.getText().split(" ");
                        Key key = keyService.getKeyById(Long.valueOf(words[1]));
                        sendMessage(chatId, keyService.formatKeyInfo(key));

                    } else if (message.getText().contains("Удалить ключ")) {
                        String[] words = message.getText().split(" ");
                        keyService.deleteKey(Long.valueOf(words[2]));

                    }
                    break;
            }


        }
        else{
            switch (message.getText()) {
                case "/start":
                    sendMessage(chatId, helloForUser);
                    break;

                case "Запрос ключа":
                    sessionManager.startSession(chatId, new KeyRequestSession(new KeyRequest(chatId,)));
                    userService.setCondition(REQUEST_KEY, chatId);
                    sendMessage(chatId, "Введите номер аудитории: ");
                    break;

                case "Отправить сообщение администратору":
                    userService.setCondition(SEND_MESSAGE_TO_ADMIN, chatId);
                    sendMessage(chatId, "Введите сообщение: ");
                    break;
            }
        }
    }

   public void sendMessageToAdmin(Message message){
        if(message.getText().equals("Отмена") || message.getText().equals("отмена")){
            sendMessage(message.getChatId(), "Отправка сообщения отменена");
            userService.setCondition(NONE, message.getChatId());
            return;
        }

        userService.setCondition(NONE, message.getChatId());
        sendMessage(adminId, message.getText());
        sendMessage(message.getChatId(), "Сообщение отправлено");

   }

    public void createRequestKey(Message message) {
        Long chatId = message.getChatId();
        KeyRequestSession keyRequestSession = (KeyRequestSession) sessionManager.getSession(chatId);
        if (keyRequestSession == null) {
            sendMessage(chatId, "Сессия не найдена. Начните запрос заново.");
            return;
        }

        KeyRequest keyRequest = keyRequestSession.getKeyRequest();

        switch (keyRequestSession.getStep()) {
            case 0:
                // Шаг 0: Получаем аудиторию по ID из сообщения
                Long auditoriumId;
                try {
                    auditoriumId = Long.valueOf(message.getText());
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Пожалуйста, введите корректный ID аудитории.");
                    return;
                }

                Auditorium auditorium = auditoriumService.auditoriumRepository.getAuditoriumById(auditoriumId);
                if (auditorium == null) {
                    sendMessage(chatId, "Аудитория с таким ID не найдена. Попробуйте снова.");
                    return;
                }

                keyRequest.setAuditorium(auditorium);
                sendMessage(chatId, "Введите время возврата ключа в формате HH:mm dd/MM/yyyy");
                keyRequestSession.nextStep();
                break;

            case 1:
                // Шаг 1: Получаем время возврата ключа
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                LocalDateTime expectedReturnTime;
                try {
                    expectedReturnTime = LocalDateTime.parse(message.getText(), formatter);
                } catch (DateTimeParseException e) {
                    sendMessage(chatId, "Неверный формат даты. Пожалуйста, введите время в формате HH:mm dd/MM/yyyy");
                    return;
                }

                // Проверяем, что время возврата не в прошлом (с запасом 10 минут)
                LocalDateTime now = LocalDateTime.now();
                if (expectedReturnTime.isBefore(now.minusMinutes(10))) {
                    sendMessage(chatId, "Время возврата не может быть в прошлом. Попробуйте снова.");
                    return;
                }

                keyRequest.setExpectedReturnTime(expectedReturnTime);

                // Проверяем наличие доступных ключей
                Long auditoriumIdFromRequest = keyRequest.getAuditorium().getId();
                Key availableKey = keyService.getAvailableKeys(auditoriumIdFromRequest);
                if (availableKey == null) {
                    sendMessage(chatId, "Аудитория занята, попробуйте позже.");
                    return;
                }

                // Здесь нужно получить userId и adminId из контекста (например, из message или сессии)
                Long userId = chatId;

                // Создаем запрос на ключ
                keyRequestService.createRequestKey(chatId, keyRequest.getAuditorium().getId(), expectedReturnTime);

                sendMessage(chatId, "Запрос отправлен администратору.");
                sendMessage(adminId, "Пришел новый запрос на выдачу ключа.");

                // Завершаем сессию
                userService.setCondition(NONE, message.getChatId());
                sessionManager.endSession(chatId);
                break;

            default:
                sendMessage(chatId, "Произошла ошибка. Начните запрос заново.");
                sessionManager.endSession(chatId);
                userService.setCondition(NONE, message.getChatId());
                break;
        }
    }

    public void createKey(Message message) {
        KeySession keySession = (KeySession) sessionManager.getSession(message.getChatId());

        if (keySession == null){
            sendMessage(message.getChatId(), "Сессия не найдена, начните заново");
            userService.setCondition(NONE, message.getChatId());
            }

        if (message.getText().equalsIgnoreCase("отмена")) {
            sessionManager.endSession(message.getChatId());
            sendMessage(message.getChatId(), "Операция создания ключа отменена.");
            userService.setCondition(NONE, message.getChatId());
            return;
        }

        switch (keySession.getStep()) {
            case 0:
                // Сохраняем номер ключа
                keySession.getKey().setId(Long.valueOf(message.getText()));
                sendMessage(message.getChatId(), "Введите ID аудитории для ключа:");
                keySession.nextStep();
                break;

            case 1:
                Long auditoriumId;
                try {
                    auditoriumId = Long.valueOf(message.getText());
                } catch (NumberFormatException e) {
                    sendMessage(message.getChatId(), "Пожалуйста, введите корректный числовой ID аудитории.");
                    return;
                }

                Auditorium auditorium = auditoriumService.auditoriumRepository.getAuditoriumById(auditoriumId);
                if (auditorium == null) {
                    sendMessage(message.getChatId(), "Аудитория с таким ID не найдена. Попробуйте снова.");
                    return;
                }

                keySession.getKey().setAuditorium(auditorium);
                keyRepository.save(keySession.getKey());
                userService.setCondition(NONE, message.getChatId());
                sendMessage(message.getChatId(), "Ключ успешно создан и сохранен.");
                sessionManager.endSession(message.getChatId());
                break;

            default:
                sendMessage(message.getChatId(), "Произошла ошибка. Начните создание ключа заново.");
                sessionManager.endSession(message.getChatId());
                userService.setCondition(NONE, message.getChatId());
                break;
        }
    }

    public void createAuditorium(Message message){
        Long chatId = message.getChatId();

        AuditoriumSession auditoriumSession = (AuditoriumSession) sessionManager.getSession(chatId);
        if (message.getText().equalsIgnoreCase("отмена")) {
            sessionManager.endSession(chatId);
            userService.setCondition(NONE, message.getChatId());
            sendMessage(chatId, "Операция создания аудитории отменена.");
            return;
        }

        switch (auditoriumSession.getStep()) {
            case 0:
                // Шаг 0: сохраняем номер аудитории
                Auditorium auditorium = new Auditorium();
                auditorium.setId(Long.valueOf(message.getText()));
                auditoriumSession.setAuditorium(auditorium);

                sendMessage(chatId, "Введите название аудитории:");
                auditoriumSession.nextStep();
                break;

            case 1:
                // Шаг 1: сохраняем название аудитории
                Auditorium auditorium1 = ((AuditoriumSession) sessionManager.getSession(chatId)).getAuditorium();
                auditorium1.setName(message.getText());

                auditoriumService.auditoriumRepository.saveAndFlush(auditorium1);
                sendMessage(chatId, "Аудитория и ключ успешно созданы и сохранены.");
                sessionManager.endSession(chatId);
                userService.setCondition(NONE, message.getChatId());
                break;

            default:
                sendMessage(chatId, "Произошла ошибка. Начните создание аудитории заново.");
                sessionManager.endSession(chatId);
                userService.setCondition(NONE, message.getChatId());
                break;
        }

    }

    public void updateKey(Message message){
        Long chatId = message.getChatId();
        String text = message.getText().trim();

        KeySession keySession = (KeySession) sessionManager.getSession(chatId);

        if (text.equalsIgnoreCase("отмена")) {
            sessionManager.endSession(chatId);
            userService.setCondition(NONE, message.getChatId());
            sendMessage(chatId, "Операция обновления ключа отменена.");
            return;
        }

        switch (keySession.getStep()) {
            case 2:
                // Шаг 2: сохраняем новый ID ключа
                try {
                    Optional<Key> optionalKey = keyRepository.findById(text);
                    keySession.setKey(optionalKey.get());
                    sendMessage(chatId, "Введите ID аудитории, к которой нужно привязать ключ:");
                    keySession.nextStep();
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Пожалуйста, введите корректный числовой ID ключа.");
                }
                break;

            case 3:
                // Шаг 3: сохраняем ID аудитории
                try {
                    Long auditoriumId = Long.valueOf(text);
                    Auditorium auditorium = auditoriumService.auditoriumRepository.getAuditoriumById(auditoriumId);
                    if (auditorium == null) {
                        sendMessage(chatId, "Аудитория с таким ID не найдена. Попробуйте снова:");
                        return;
                    }

                    Key keyToUpdate = keySession.getKey();
                    keyToUpdate.setAuditorium(auditorium);
                    keyRepository.saveAndFlush(keyToUpdate);

                    sendMessage(chatId, "Ключ успешно обновлён и привязан к аудитории.");
                    userService.setCondition(NONE, message.getChatId());
                    sessionManager.endSession(chatId);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Пожалуйста, введите корректный числовой ID аудитории.");
                }
                break;

            default:
                sendMessage(chatId, "Произошла ошибка. Начните обновление ключа заново.");
                sessionManager.endSession(chatId);
                userService.setCondition(NONE, message.getChatId());
                break;
        }
    }

   public void updateReturnTime(Message message){
       Long chatId = message.getChatId();
       String text = message.getText().trim();

       KeySession keySession = (KeySession) sessionManager.getSession(chatId);
       if (keySession == null) {
           sendMessage(chatId, "Сессия не найдена. Пожалуйста, начните заново.");
           userService.setCondition(NONE, message.getChatId());
           return;
       }

       int currentStep = keySession.getStep();

       if (currentStep == 2) {
           // Шаг 2: получить ID ключа, найти ключ, сохранить в сессию, запросить время
           try {
               Long keyId = Long.valueOf(text);
               Optional<Key> optionalKey = keyRepository.findById(String.valueOf(keyId));
               if (optionalKey.isEmpty()) {
                   sendMessage(chatId, "Ключ с таким ID не найден. Введите корректный ID ключа:");
                   return;
               }
               Key key = optionalKey.get();
               keySession.setKey(key);
               keySession.setStep(3);

               sendMessage(chatId, "Введите время в формате HH:mm dd/MM/yyyy:");
           } catch (NumberFormatException e) {
               sendMessage(chatId, "Введите корректный числовой ID ключа:");
           }
       } else if (currentStep == 3) {
           // Шаг 3: получить время, обновить ключ и keyRequest, сохранить
           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
           try {
               LocalDateTime dateTime = LocalDateTime.parse(text, formatter);

               Key key = keySession.getKey();
               if (key == null) {
                   sendMessage(chatId, "Ошибка: ключ не найден в сессии. Начните заново.");
                   sessionManager.endSession(chatId);
                   return;
               }

               // Обновляем время в ключе
               key.setReturnTime(dateTime);
               keyRepository.saveAndFlush(key);

               // Обновляем время в KeyRequest
               KeyRequestSession keyRequestSession = (KeyRequestSession) sessionManager.getSession(chatId);
               if (keyRequestSession != null) {
                   KeyRequest keyRequest = keyRequestSession.getKeyRequest();
                   if (keyRequest != null) {
                       keyRequest.setExpectedReturnTime(dateTime);
                       keyRequestRepository.saveAndFlush(keyRequest);
                   }
               }

               sendMessage(chatId, "Время успешно обновлено.");
               userService.setCondition(NONE, message.getChatId());
               sessionManager.endSession(chatId);

           } catch (DateTimeParseException e) {
               sendMessage(chatId, "Неверный формат времени. Пожалуйста, введите время в формате HH:mm dd/MM/yyyy:");
           }
       } else {
           sendMessage(chatId, "Неожиданный шаг. Пожалуйста, начните заново.");
           userService.setCondition(NONE, message.getChatId());
           sessionManager.endSession(chatId);
       }
   }

   public void updateNameAuditorium(Message message){
        if(message.getText().toLowerCase().equals("отмена")){
            sendMessage(message.getChatId(), "Операция изменения названия отменена.");
            userService.setCondition(NONE, message.getChatId());
        }
       String[] words = message.getText().split(", ");

        Auditorium auditorium = auditoriumService.auditoriumRepository.getAuditoriumById(Long.valueOf(words[0]));

        auditorium.setName(words[1]);

        auditoriumService.auditoriumRepository.saveAndFlush(auditorium);
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


