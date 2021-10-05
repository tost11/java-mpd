package de.tostsoft.mpdclient.modules;

import de.tostsoft.mpdclient.modules.interfaces.BasicResultListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerCommandResult {
    private String command;
    private static int ID_COUNT = 1;
    private int commandId = ID_COUNT++ ;
    private ArrayList<String> results = new ArrayList<>();
    private List<BasicResultListener> listeners = new ArrayList<>();
    private boolean isFinished = false;
    private Lock lock = new ReentrantLock();

    public PlayerCommandResult(String command){
        this.command = command;
        lock.lock();//lock until finished
    }

    public String getCommand(){
        return command;
    }

    public void addResult(String s){
        results.add(s);
    }

    public ArrayList<String> getResults(){
        return results;
    }

    public int getId(){
        return commandId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == String.class){
            String s = (String)obj;
            return s.equals(command);
        }else{
            PlayerCommandResult r = (PlayerCommandResult)obj;
            return command.equals(r.getCommand());
        }
    }

    public List<BasicResultListener> getListeners(){return listeners;}

    public boolean isFinished(){
        return isFinished;
    }

    public void setFinished(boolean finished){
        isFinished = finished;
        lock.unlock();
    }

    public void waitForCompletion(){
        //this look stupid but it is not
        lock.lock();
        lock.unlock();
    }
}
