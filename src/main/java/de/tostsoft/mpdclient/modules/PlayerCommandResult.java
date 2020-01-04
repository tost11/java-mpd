package de.tostsoft.mpdclient.modules;

import java.util.ArrayList;

public class PlayerCommandResult {
    private String command;
    private static int ID_COUNT = 1;
    private int commandId = ID_COUNT++ ;
    private ArrayList<String> results = new ArrayList<>();

    public PlayerCommandResult(String command){
        this.command = command;
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
}
