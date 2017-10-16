import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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
    private String      adminPassword       = "password";
    private String      startCode           = "startcode";
    private int         startYear           = 2017;
    private int         startMonth          = 9;
    private int         startDay            = 16;
    private int         startHour           = 20;
    private int         startMinute         = 25;
    private long        twentyMinutesMilli  = 1200000;
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
        LocalTime currentTime = LocalTime.now();
        LocalDateTime currentDateTime = LocalDateTime.now();
// We check if the update has a message and the message has text
        if (message != null && message.hasText()){

            if (!isGameStarted){
                cmdChecking(message);
            }

            if (message.getText().equals("/time")){
                String sTime = currentDateTime.getHour() + ":" + addZero(currentDateTime.getMinute());
                sendMsg(message, sTime);
            }

            if (message.getText().equals("/date")){
                String sDate = currentDateTime.getDayOfMonth() + "." + addZero(currentDateTime.getMonthValue())
                        + "." + currentDateTime.getYear();
                sendMsg(message, sDate);
            }

            if (message.getText().equals("/chatid")){
                sendMsg(message, String.valueOf(message.getChatId()));
            }
            if (message.getText().equals("/when")){
                tasksFile = tasksAccess.getTasks();
                getStartDateTime();
                String sTime = addZero(startHour) + ":" + addZero(startMinute);
                String sDate = startDay + "." + addZero(startMonth) + "." + startYear;
                sendMsg(message, "Игра начнется в " + sDate + " в " + sTime);
            }

            if (message.getText().equals("/gl")){
                tasksFile = tasksAccess.getTasks();
                sendMsg(message, tasksFile.getProperty(GAME_LEGEND));
            }

            if ((currentTime.getHour() >= startHour) || (currentTime.getHour() < 8)) {
                if (isGameStarted && isChatInGame(message)){
                    int index = gameDataIndex(message);
                    String key = TASK_CODE + "_" + gameDataList.get(index).getTaskNumber();
                    String taskCode = tasksFile.getProperty(key);
                    if (message.getText().equals(taskCode)){
                        sendMsg(message, "Вы ответили правильно");
                        int taskNumber = gameDataList.get(index).getTaskNumber() + 1;
                        if (taskNumber < 8){
                            gameDataList.get(index).setTaskNumber(taskNumber);
                            gameDataList.get(index).setHintNumber(1);
                            taskTimerList.get(index).cancel();
                            key = TASK + "_" + taskNumber;
                            sendMsg(message, tasksFile.getProperty(key));
                            taskTimerList.set(index, new Timer());
                            tasksTimer(message);
                        } else {
                            taskTimerList.get(index).cancel();
                            gameOver(message);
                        }
                    }
                }
            }
        }
    }

    //Checking input commands
    private void cmdChecking(Message message){
        //Checking. Message is a command?
        isStartCommand      = (message.getText().equals("/start") || message.getText().equals("/Start"));
        isAdminCommand      = (message.getText().equals("/admin") || message.getText().equals("/Admin"));
        isChatIdCommand     = (message.getText().equals("/chatid") || message.getText().equals("/Chatid"));
        isStartCodeCommand  = (message.getText().equals("/sc") || message.getText().equals("/Sc")) ||
                message.getText().equals("/StartCode");
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
        startTimer(message);
    }

    //Timer tracking the start day and time
    private void startTimer(final Message message){
        LocalDateTime gameDateTime = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute);
        ZoneId zoneId = ZoneId.systemDefault();
        long presetTime = gameDateTime.atZone(zoneId).toEpochSecond() * 1000;

        try{
            Timer newTimer = new Timer();
            newTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendMsg(message, tasksFile.getProperty(GAME_LEGEND));
                    sendMsg(message, tasksFile.getProperty(TASK + "_" + "1"));
                    isGameStarted = true;
                    gameDataList.get(gameDataIndex(message)).setHintNumber(1);
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
                        hintNumber = 1;
                        gameDataList.get(index).setHintNumber(hintNumber);
                        taskNumber++;
                        isTaskTimer = false;
                        isHintTimer = true;
                        if (/*!isGameEnded &&*/ taskNumber < 8){
                            String key = TASK + "_" + taskNumber;
                            sendMsg(message, tasksFile.getProperty(key));
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
        LocalTime endTime = LocalTime.now();
        int endHour = endTime.getHour();
        int endMinute = endTime.getMinute();
        int penaltyTime = gameDataList.get(gameDataIndex(message)).getPenaltyTime_min();
        if (startHour > endHour){
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
        }
        isHintTimer = false;
        isTaskTimer = false;
        isGameEnded = true;
    }
    //Return the last digit of a number
    private int lastDigit(int value){
        int tempValue = value;
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
}