import java.io.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendVideo;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class ANGBot extends TelegramLongPollingBot {
    private boolean     isStart;
    private boolean     isTimer;
    private boolean     isAdminMode         = false;
    private boolean     isAdminPassword     = false;
    private boolean     isCommand           = false;
    private boolean     isStartCommand      = false;
    private boolean     isAdminCommand      = false;
    private boolean     isStartCodeMode     = false;
    private boolean     isFirstStartCode    = false;
    private boolean     isStartCodeCommand  = false;
    private boolean     isChatIdCommand     = false;
    private boolean     isGameStarted       = false;
    private boolean     isGameEnded         = false;
    private boolean     isTaskTimer         = false;
    private boolean     isHintTimer         = false;
    private boolean     isBonusFiveUsed     = false;
    private boolean     isBonusTenUsed      = false;
    private boolean     isBonusFifteenUsed  = false;
    private String      adminPassword       = "password";
    private String      startCode           = "startcode";
    private int         startYear           = 2017;
    private int         startMonth          = 9;
    private int         startDay            = 16;
    private int         startHour           = 20;
    private int         startMinute         = 25;
    private long        twentyMinutesMilli  = 1200000; //1200000
    private ZoneId      zoneId              = ZoneId.of("Europe/Moscow");
    private ArrayList<Long>     chatIdList          = new ArrayList<Long>();
    private ArrayList<GameData> gameDataList        = new ArrayList<GameData>();
    private ArrayList<Timer>    taskTimerList       = new ArrayList<Timer>();
    private TasksAccess tasksAccess = new TasksAccess();
    private Properties tasksFile;
    //Keys for properties
    private final String START_DATE          = "START_DATE";
    private final String START_TIME          = "START_TIME";
    private final String START_CODE          = "START_CODE";
    private final String GAME_LEGEND         = "GAME_LEGEND";
    private final String TASK                = "TASK";
    private final String HINT                = "HINT";
    private final String TASK_CODE           = "TASK_CODE";

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        LocalTime currentTime = LocalTime.now(zoneId);
        LocalDateTime currentDateTime = LocalDateTime.now(zoneId);
// We check if the update has a message and the message has text
        if (message != null && message.hasText()){

            if (!isGameStarted){
                cmdChecking(message);
            }

            //Отображение пути к jar
            if (message.getText().equalsIgnoreCase("/path")){
                ApplicationStartUpPath startUpPath = new ApplicationStartUpPath();
                try {
                    sendMsg(message, "startUpPath: " + startUpPath.getApplicationStartUp());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (message.getText().equalsIgnoreCase("/time")){
                String sTime = currentDateTime.getHour() + ":" + addZero(currentDateTime.getMinute());
                sendMsg(message, sTime);
            }

            if (message.getText().equalsIgnoreCase("/vid")){
                sendVid(message, "vid");
            }

            if (message.getText().equalsIgnoreCase("/date")){
                String sDate = currentDateTime.getDayOfMonth() + "." + addZero(currentDateTime.getMonthValue())
                        + "." + currentDateTime.getYear();
                sendMsg(message, sDate);
            }

            if (message.getText().equalsIgnoreCase("/chatid")){
                sendMsg(message, String.valueOf(message.getChatId()));
            }
            if (message.getText().equalsIgnoreCase("/when")){
                tasksFile = tasksAccess.getTasks();
                getStartDateTime();
                String sTime = addZero(startHour) + ":" + addZero(startMinute);
                String sDate = startDay + "." + addZero(startMonth) + "." + startYear;
                sendMsg(message, "Игра начнется в " + sDate + " в " + sTime);
            }

            if (message.getText().equalsIgnoreCase("/gl")){
                tasksFile = tasksAccess.getTasks();
                sendMsg(message, tasksFile.getProperty(GAME_LEGEND));
                sendImg(message, "legend");
                sendVid(message, "legend");
            }

            if ((currentTime.getHour() >= startHour) || (currentTime.getHour() < 8)) {
                if (isGameStarted && isChatInGame(message)){
                    int index = gameDataIndex(message);
                    String key = TASK_CODE + "_" + gameDataList.get(index).getTaskNumber();
                    String taskCode = tasksFile.getProperty(key);
                    String bonusFiveMinutes = tasksFile.getProperty("BONUS_FIVE_MINUTES");
                    String bonusTenMinutes = tasksFile.getProperty("BONUS_TEN_MINUTES");
                    String bonusFifteenMinutes = tasksFile.getProperty("BONUS_FIFTEEN_MINUTES");
                    if (message.getText().equalsIgnoreCase(taskCode)){
                        sendMsg(message, "Вы ввели верный код");
                        int taskNumber = gameDataList.get(index).getTaskNumber() + 1;
                        if (taskNumber < 8){
                            gameDataList.get(index).setTaskNumber(taskNumber);
                            gameDataList.get(index).setHintNumber(1);
                            gameDataList.get(index).setTaskTime();
                            taskTimerList.get(index).cancel();
                            key = TASK + "_" + taskNumber;
                            sendMsg(message, tasksFile.getProperty(key));
                            String fileName = "task_" + taskNumber;
                            sendImg(message, fileName);
                            sendVid(message, fileName);
                            taskTimerList.set(index, new Timer());
                            tasksTimer(message);
                        } else {
                            gameDataList.get(index).setTaskTime();
                            taskTimerList.get(index).cancel();
                            gameOver(message);
                        }
                    } else if (!isBonusFiveUsed && message.getText().equalsIgnoreCase(bonusFiveMinutes)){
                        int bonusTime = gameDataList.get(index).getBonusTime_min() + 5;
                        gameDataList.get(index).setBonusTime_min(bonusTime);
                        gameDataList.get(index).setBonusFiveMinutes(true);
                        isBonusFiveUsed = true;
                        sendMsg(message, "Вы активировали бонусный код.\nОт итогового времени отнимется 5 минут.");
                    } else if (!isBonusTenUsed && message.getText().equalsIgnoreCase(bonusTenMinutes)){
                        int bonusTime = gameDataList.get(index).getBonusTime_min() + 5;
                        gameDataList.get(index).setBonusTime_min(bonusTime);
                        gameDataList.get(index).setBonusTenMinutes(true);
                        isBonusTenUsed = true;
                        sendMsg(message, "Вы активировали бонусный код.\nОт итогового времени отнимется 5 минут.");
                    } else if (!isBonusFifteenUsed && message.getText().equalsIgnoreCase(bonusFifteenMinutes)){
                        int bonusTime = gameDataList.get(index).getBonusTime_min() + 5;
                        gameDataList.get(index).setBonusTime_min(bonusTime);
                        gameDataList.get(index).setBonusFifteenMinutes(true);
                        isBonusFifteenUsed = true;
                        sendMsg(message, "Вы активировали бонусный код.\nОт итогового времени отнимется 5 минут.");
                    }
                }
            }
        }
    }

    //Checking input commands
    private void cmdChecking(Message message){
        //Checking. Message is a command?
        isStartCommand      = (message.getText().equalsIgnoreCase("/start"));
        isAdminCommand      = (message.getText().equalsIgnoreCase("/admin"));
        isChatIdCommand     = (message.getText().equalsIgnoreCase("/chatid"));
        isStartCodeCommand  = (message.getText().equalsIgnoreCase("/sc"));
        isCommand           = (isStartCommand || isAdminCommand || isChatIdCommand || isStartCodeCommand);


        if (!isAdminMode && isStartCommand){
            isStart = true;
            sendMsg(message, "Здравствуйте, вас приветствует команда Alternative Night Game.");
        } else {
            if (!isAdminMode && isChatIdCommand){
                //sendMsg(message, String.valueOf(message.getChatId()));
            } else {
                if (!isAdminMode && !isStartCodeMode && isAdminCommand){
                    isAdminMode = true;
                    sendMsg(message, "Вы пытаетесь войти в режим администратора. Пожалуйста введите пароль.\n" +
                            "Если вы хотите выйти из режима администратора, введите комманду /admin");
                } else if (isAdminCommand && isAdminMode){
                    isAdminMode = false;
                    sendMsg(message, "Вы вышли из режима администратора.");
                } else if (message.getText().equals(adminPassword) && isAdminMode){

                } else if (!message.getText().equals(adminPassword) && isAdminMode && !isAdminCommand){
                    sendMsg(message, "Вы ввели неверный пароль. Попробуйте еще раз.\n" +
                            "Введите /admin что-бы выйти из этого режима.");
                } else if (!isAdminMode){
                    if (isStartCodeCommand && !isStartCodeMode){
                        if (!isFirstStartCode){
                            tasksFile = tasksAccess.getTasks();
                            startCode = tasksFile.getProperty(START_CODE);
                            isFirstStartCode = true;
                        }
                        isStartCodeMode = true;
                        sendMsg(message,"Введите стартовый код.");
                    } else if (message.getText().equals(startCode) && isStartCodeMode){
                        if (gameDataList.isEmpty() || !isChatInGame(message)){
                            createGameData(message);
                            String sTime = startHour + ":" + addZero(startMinute);
                            sendMsg(message, "Вы ввели правильный код.");
                            sendMsg(message, "Игра начнется в " + sTime);
                        } else if (isChatInGame(message)){
                            sendMsg(message, "Вы уже ввели правильный код");
                        }
                        isStartCodeMode = false;
                    } else if (!message.getText().equals(startCode) && isStartCodeMode && !isStartCodeCommand){
                        isStartCodeMode = false;
                        sendMsg(message, "Вы ввели неверный стартовый код. Попробуйте еще раз.");
                    }
                }
            }
        }
    }

    //Creating game data objects
    private void createGameData(Message message){
        chatIdList.add(message.getChatId());
        gameDataList.add(new GameData(message.getChatId()));
        taskTimerList.add(new Timer());
        getStartDateTime();
        legendTimer(message);
        startTimer(message);
    }

    //Таймер для вывода легенды игры, за 5 минут до начала.
    private void legendTimer(final Message message){
        LocalDateTime gameDateTime = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute);
        long presetTime = gameDateTime.atZone(zoneId).toEpochSecond() * 1000 - 300000;

        try{
            Timer newTimer = new Timer();
            newTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendMsg(message, tasksFile.getProperty(GAME_LEGEND));
                    sendImg(message, "legend");
                    sendVid(message, "legend");
                }
            }, new Date(presetTime));
        } catch (IllegalArgumentException e){
            sendMsg(message, "Игра уже началась");
        }
    }

    //Timer tracking the start day and time
    private void startTimer(final Message message){
        LocalDateTime gameDateTime = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute);
        //ZoneId zoneId = ZoneId.systemDefault();
        //ZoneId zoneId = ZoneId.of("Europe/Moscow");
        long presetTime = gameDateTime.atZone(zoneId).toEpochSecond() * 1000;

        try{
            Timer newTimer = new Timer();
            newTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int index = gameDataIndex(message);
                    sendMsg(message, tasksFile.getProperty(TASK + "_" + "1"));
                    sendImg(message, "task_1");
                    sendVid(message, "task_1");
                    isGameStarted = true;
                    gameDataList.get(index).setTaskTime();
                    gameDataList.get(index).setHintNumber(1);
                    isHintTimer = true;
                    tasksTimer(message);
                }
            }, new Date(presetTime));
        } catch (IllegalArgumentException e){
            sendMsg(message, "Игра уже началась");
        }
    }

    //Tasks timer
    private void tasksTimer(final Message message){
        try{
            Timer taskTimer = taskTimerList.get(gameDataIndex(message));
            taskTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int index = gameDataIndex(message);
                    int taskNumber = gameDataList.get(index).getTaskNumber();
                    int hintNumber = gameDataList.get(index).getHintNumber();
                    if (hintNumber > 0 && hintNumber < 3){
                        String key = HINT + "_" + taskNumber + "_" + hintNumber;
                        sendMsg(message, tasksFile.getProperty(key));
                        hintNumber++;
                        gameDataList.get(index).setHintNumber(hintNumber);
                        if (hintNumber == 3){
                            isHintTimer = false;
                            isTaskTimer = true;
                        }
                        tasksTimer(message);
                    } else if (hintNumber == 3) {
                        int penaltyTime = gameDataList.get(index).getPenaltyTime_min() + 15;
                        gameDataList.get(index).setPenaltyTime_min(penaltyTime);
                        gameDataList.get(index).setTaskTime();
                        hintNumber = 1;
                        gameDataList.get(index).setHintNumber(hintNumber);
                        taskNumber++;
                        isTaskTimer = false;
                        isHintTimer = true;
                        sendMsg(message, "Вы не нашли верный код.\nДобавлено штрафное время, 15 минут.");
                        if (/*!isGameEnded &&*/ taskNumber < 8){
                            String key = TASK + "_" + taskNumber;
                            sendMsg(message, tasksFile.getProperty(key));
                            String fileName = "task_" + taskNumber;
                            sendImg(message, fileName);
                            sendVid(message, fileName);
                            gameDataList.get(index).setTaskNumber(taskNumber);
                            tasksTimer(message);
                        } else if (taskNumber == 8){
                            gameOver(message);
                        }
                    }
                }
            }, twentyMinutesMilli);
        } catch (IllegalArgumentException e){
            sendMsg(message, "Ошибка создания таймера");
        }
    }

    //Game End
    private void gameOver(Message message){
        int index = gameDataIndex(message);
        LocalTime endTime = LocalTime.now();
        int endHour = endTime.getHour();
        int endMinute = endTime.getMinute();
        //Штрафное и бонусное время в секундах
        long penaltyTime = gameDataList.get(index).getPenaltyTime_min() * 60;
        long bonusTime = gameDataList.get(index).getBonusTime_min() * 60;

        /*if (startHour > endHour){
            endHour = endHour + 24;
        }
        int totalTime = endHour * 60 + endMinute + penaltyTime - startHour * 60 - startMinute;
        String text = "Игра закончена." + "\n" + "Вы прошли игру за " + totalTime;
        if (lastDigit(totalTime) == 1){
            sendMsg(message, text + " минуту.");
        } else if (lastDigit(totalTime) > 4 || lastDigit(totalTime) == 0){
            sendMsg(message, text + " минут.");
        } else {
            sendMsg(message, text + " минуты.");
        }*/

        String tasksSummary = "Время затраченное на выполнение заданий.\n\n";
        ArrayList<LocalDateTime> tempTaskMarks = gameDataList.get(index).getTaskMarks();
        long taskSeconds;
        int size = tempTaskMarks.size();
        for (int i = 0; i < size - 1; i++){
            taskSeconds = Duration.between(tempTaskMarks.get(i), tempTaskMarks.get(i + 1)).getSeconds();
            tasksSummary = tasksSummary + "Задание " + String.valueOf(i + 1) + " - " + timeString(taskSeconds) + "\n";
        }

        tasksSummary = tasksSummary + "Штрафное время " + timeString(penaltyTime) + "\n";
        tasksSummary = tasksSummary + "Бонусное время " + timeString(bonusTime) + "\n";
        taskSeconds = Duration.between(tempTaskMarks.get(0), tempTaskMarks.get(size - 1)).getSeconds() + penaltyTime - bonusTime;
        tasksSummary = tasksSummary + "Итого игра заняла у вас " + timeString(taskSeconds) + "\n\n" + "Благодарим вас за участие.";
        sendMsg(message, tasksSummary);

        isHintTimer = false;
        isTaskTimer = false;
        isGameEnded = true;
    }

    public String timeString(long taskSeconds){
        long taskMinutes;
        long taskHours;
        String text = "";

        taskMinutes = taskSeconds / 60;
        taskHours = taskMinutes / 60;
        taskSeconds = taskSeconds % 60;
        taskMinutes = taskMinutes % 60;

        text = text + addZero(taskHours) + ":" + addZero(taskMinutes) + ":" + addZero(taskSeconds);

        /*
        if (lastDigit(taskHours) == 1){
            text = text + taskHours + " час, ";
        } else if (lastDigit(taskHours) > 4 || lastDigit(taskHours) == 0){
            text = text + taskHours + " часов, ";
        } else {
            text = text + taskHours + " часа, ";
        }

        if (lastDigit(taskMinutes) == 1){
            text = text + taskMinutes + " минуту, ";
        } else if (lastDigit(taskMinutes) > 4 || lastDigit(taskMinutes) == 0){
            text = text + taskMinutes + " минут, ";
        } else {
            text = text + taskMinutes + " минуты, ";
        }

        if (lastDigit(taskSeconds) == 1){
            text = text + taskSeconds + " секунду.";
        } else if (lastDigit(taskSeconds) > 4 || lastDigit(taskSeconds) == 0){
            text = text + taskSeconds + " секунд.";
        } else {
            text = text + taskSeconds + " секунды.";
        }
        */

        return text;
    }

    //Return the last digit of a number
    private long lastDigit(long value){
        long tempValue = value;
        if (value > 9){
            value = value % 10;
            tempValue = tempValue % 100;
            if (tempValue > 10 && tempValue < 20){
                value = tempValue;
            }
        }
        return  value;
    }

    //If number < 10, add 0
    private String addZero(int value){
        String answer = "0";
        if (value < 10){
            answer = answer + value;
        } else {
            answer = String.valueOf(value);
        }
        return  answer;
    }

    private String addZero(long value){
        String answer = "0";
        if (value < 10){
            answer = answer + value;
        } else {
            answer = String.valueOf(value);
        }
        return  answer;
    }

    private boolean isChatInGame(Message message){
        int index;
        boolean answer = false;
        for (index = 0; index < gameDataList.size(); index++){
            if (gameDataList.get(index).getChat_id() == message.getChatId()){
                answer = true;
                break;
            }
        }
        return answer;
    }

    //Searching relevant game object in the array
    private int gameDataIndex(Message message){
        int index;
        for (index = 0; index < gameDataList.size(); index++){
            if (gameDataList.get(index).getChat_id() == message.getChatId()){
                break;
            }
        }
        return index;
    }

    //Get date and time variables
    private void getStartDateTime(){
        String fullDate = tasksFile.getProperty(START_DATE);
        String fullTime = tasksFile.getProperty(START_TIME);
        startDay = Integer.valueOf(fullDate.substring(0, 2));
        startMonth = Integer.valueOf(fullDate.substring(3, 5));
        startYear = Integer.valueOf(fullDate.substring(6, 10));
        startHour = Integer.valueOf(fullTime.substring(0, 2));
        startMinute = Integer.valueOf(fullTime.substring(3, 5));
    }

    //Sending messages
    private void sendMsg(Message message, String text){
        SendMessage sendMessage = new SendMessage(); // Create a message object object
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        try {
            execute(sendMessage); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //Sending images
    private void sendImg(Message message, String text){
        String sFileName = text + ".jpg";
        String sDirSeparator = System.getProperty("file.separator");
        ApplicationStartUpPath startUpPath = new ApplicationStartUpPath();
        String sFilePath = "";

        try {
            sFilePath = startUpPath.getApplicationStartUp() + sDirSeparator + sFileName;
        } catch (IOException e) {
            e.printStackTrace();
        }

        SendPhoto sendMyPhoto = new SendPhoto();
        sendMyPhoto.setChatId(message.getChatId());
        sendMyPhoto.setNewPhoto(new File(sFilePath));

        try {
            sendPhoto(sendMyPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //Sending videos
    private void sendVid(Message message, String text){
        String sFileName = text + ".mp4";
        String sDirSeparator = System.getProperty("file.separator");
        ApplicationStartUpPath startUpPath = new ApplicationStartUpPath();
        String sFilePath = "";

        try {
            sFilePath = startUpPath.getApplicationStartUp() + sDirSeparator + sFileName;
        } catch (IOException e) {
            e.printStackTrace();
        }

        SendVideo sendMyVideo = new SendVideo();
        sendMyVideo.setChatId(message.getChatId());
        sendMyVideo.setNewVideo(new File(sFilePath));

        try {
            sendVideo(sendMyVideo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        // Return bot username
        // If bot username is @MyAmazingBot, it must return 'MyAmazingBot'
        return "ANGquestbot";
    }

    @Override
    public String getBotToken() {
        // Return bot token from BotFather
        return "400297140:AAH_EqzOCmZCya4AHJ6hgqIaGY4Ky9jLupE";
    }

    class GameObject {
        private int     taskNumber                  = 1;
        private int     hintNumber                  = 0;
        private int     totalTime                   = 0;
        private int     penaltyTime_min             = 0;
        private int     bonusTime_min               = 0;
        private long    chatId;
        private boolean isBonusFiveMinutes          = false;
        private boolean isBonusTenMinutes           = false;
        private boolean isBonusFifteenMinutes       = false;
        private ArrayList<LocalDateTime> taskMarks  = new ArrayList<>();

        GameObject (long value) {chatId = value;}


    }
}