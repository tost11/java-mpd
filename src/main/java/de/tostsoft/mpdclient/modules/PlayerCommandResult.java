package de.tostsoft.mpdclient.modules;

import de.tostsoft.mpdclient.modules.interfaces.BasicResultListener;
import de.tostsoft.mpdclient.tools.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerCommandResult {
    private String command;
    private static int ID_COUNT = 1;
    private int commandId = ID_COUNT++ ;
    private ArrayList<String> results = new ArrayList<>();
    private boolean isFinished = false;
    private Semaphore semaphore = new Semaphore(1, true);
    private Boolean wasSuccesfull = true;

    public PlayerCommandResult(String command){
        this.command = command;
        try {
            semaphore.acquire(1);
        }catch (InterruptedException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,ex.getMessage());
        }
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

    public boolean isFinished(){
        return isFinished;
    }

    public void finish(){
        isFinished = true;
        semaphore.release(1);
    }

    public void setSuccessFull(boolean successFull){
        wasSuccesfull = successFull;
    }

    public boolean waitForCompletion() {
        return waitForCompletion(0,null);
    }

    public boolean waitForCompletion(long time,TimeUnit timeUnit){
        //this look stupid but it is not
        try {
            if(timeUnit != null){
                if(!semaphore.tryAcquire(time,timeUnit)){
                    return false;
                }
            }else{
                semaphore.acquire();
            }
        }catch (InterruptedException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,ex.getMessage());
        }
        semaphore.release();
        return true;
    }

    public Boolean wasSuccesfull(){
        return wasSuccesfull;
    }
}
