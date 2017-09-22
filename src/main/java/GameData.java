public class GameData {
    private int     taskNumber          = 1;
    private int     hintNumber          = 0;
    private int     totalTime           = 0;
    private int     penaltyTime_min     = 0;
    private long    chatId;

    GameData (long value) {chatId = value;}

    public void setTaskNumber(int value) {taskNumber = value;}
    public void setHintNumber(int value) {hintNumber = value;}
    public void setTotalTime(int value) {totalTime = value;}
    public void setPenaltyTime_min(int value) {penaltyTime_min = value;}
    public void setChat_id(long value) {chatId = value;}

    public  int getTaskNumber() {return  taskNumber;}
    public  int getHintNumber() {return hintNumber;}
    public  int getTotalTime() {return totalTime;}
    public  int getPenaltyTime_min() {return  penaltyTime_min;}
    public  long getChat_id() {return  chatId;}


}