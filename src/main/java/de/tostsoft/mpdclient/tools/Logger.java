package de.tostsoft.mpdclient.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Logger {
    public enum Logtype{
        FATAL_ERROR,
        ERROR,
        WARNING,
        INFO,
        DEBUG
    }

    public interface LogListener{
        public void logged(Logtype type,String message);
    }

    static private String LOGDIR = "logs";
    private FileWriter fileWriter = null;
    boolean[] activeLogTypes = new boolean[Logtype.values().length];
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private SimpleDateFormat fileDateFormater = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private List<LogListener> listeners = new ArrayList<>();

    public Logger(){
        for(int i = 0; i< activeLogTypes.length; i++){
            activeLogTypes[i]=true;
        }
        activeLogTypes[Logtype.DEBUG.ordinal()]=false;
    }

    public void setLogLevel(Logtype type,boolean enabled){
        activeLogTypes[type.ordinal()] = enabled;
    }

    public void addListener(LogListener logListener){
        listeners.add(logListener);
    }

    public void init(){
        if(fileWriter == null){
            File f = new File(LOGDIR);
            if(!f.exists()){
                try {
                    f.mkdir();
                }catch(SecurityException ex){
                    log(Logtype.ERROR,"Could not create log Directory: "+LOGDIR);
                    return;
                }
            }
            try {
                fileWriter = new FileWriter((LOGDIR.isEmpty() ? "" : LOGDIR + "/" )+ fileDateFormater.format(new Date()) + ".log");
            }catch(IOException ex){
                log(Logtype.ERROR,"Could not create Log file");
            }
        }
    }

    public synchronized void log(Logtype type,String message){
        String s = dateFormat.format(new Date()) + " ["+type+"]: "+message;
        if(activeLogTypes[type.ordinal()]){
            System.out.println(s);
        }

        if(fileWriter != null){
            try {
                fileWriter.write(s + "\n");
                fileWriter.flush();
            }catch (IOException ex){
            }
        }

        listeners.forEach(l->l.logged(type,message));
    }

    private static Logger LOGGER = null;
    static synchronized public Logger getInstance(){
        if(LOGGER == null){
            LOGGER = new Logger();
        }
        return LOGGER;
    }
}
