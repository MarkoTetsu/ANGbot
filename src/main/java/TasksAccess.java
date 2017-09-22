import java.io.*;
import java.util.Properties;

public class TasksAccess {
    private final String sFileName = "src/main/resources/tasks.properties";
    private String sDirSeparator = System.getProperty("file.separator");

    //Read properties file
    public Properties getTasks(){
        Properties props = new Properties();
        File currentDir = new File(".");
        try {
            String sFilePath = currentDir.getCanonicalPath() + sDirSeparator + sFileName;
            FileInputStream ins = new FileInputStream(sFilePath);
            props.load(ins);
            ins.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    //Rewrite properties file
    public void setTasks(Properties props){
        File currentDir = new File(".");
        try {
            String sFilePath = currentDir.getCanonicalPath() + sDirSeparator + sFileName;
            FileOutputStream outs = new FileOutputStream(sFilePath);
            props.store(outs, null);
            outs.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
