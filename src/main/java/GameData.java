import java.time.LocalDateTime;
import java.util.ArrayList;

public class GameData {
    private int     taskNumber                  = 1;
    private int     hintNumber                  = 0;
    private int     totalTime                   = 0;
    private int     penaltyTime_min             = 0;
    private int     friendCallPenalty           = 0;
    private int     bonusTime_min               = 0;
    private long    chatId;
    private boolean isFriendCall                = false;
    private boolean isBonusTask                 = false;
    private boolean isBonusTaskStart            = false;
    private boolean isBonusFiveMinutes          = false;
    private boolean isBonusTenMinutes           = false;
    private boolean isBonusFifteenMinutes       = false;
    private ArrayList<LocalDateTime> taskMarks  = new ArrayList<>();


    GameData (long value) {chatId = value;}

    public void setTaskTime(){
        taskMarks.add(LocalDateTime.now());
    }

    public ArrayList<LocalDateTime> getTaskMarks(){
        return taskMarks;
    }

    public void setTaskNumber(int value) {taskNumber = value;}
    public void setHintNumber(int value) {hintNumber = value;}
    public void setTotalTime(int value) {totalTime = value;}
    public void setPenaltyTime_min(int value) {penaltyTime_min = value;}
    public void setBonusTime_min(int value) {bonusTime_min = value;}
    public void setChat_id(long value) {chatId = value;}
    public void setFriendCall(boolean value) {isFriendCall = value;}
    public void setBonusTask(boolean value) {isBonusTask = value;}
    public void setBonusTaskStart(boolean value) {isBonusTaskStart = value;}
    public void setBonusFiveMinutes(boolean value) {isBonusFiveMinutes = value;}
    public void setBonusTenMinutes(boolean value) {isBonusTenMinutes = value;}
    public void setBonusFifteenMinutes(boolean value) {isBonusFifteenMinutes = value;}

    public  int getTaskNumber() {return  taskNumber;}
    public  int getHintNumber() {return hintNumber;}
    public  int getTotalTime() {return totalTime;}
    public  int getPenaltyTime_min() {return  penaltyTime_min;}
    public int getBonusTime_min() {return bonusTime_min;}
    public  long getChat_id() {return  chatId;}
    public boolean isFriendCall() {return  isFriendCall;}
    public boolean isBonusTask() {return  isBonusTask;}
    public boolean isBonusTaskStart() {return  isBonusTaskStart;}
    public boolean isBonusFiveMinutes() {return isBonusFiveMinutes;}
    public boolean isBonusTenMinutes() {return isBonusTenMinutes;}
    public boolean isBonusFifteenMinutes() {return isBonusFifteenMinutes;}
}