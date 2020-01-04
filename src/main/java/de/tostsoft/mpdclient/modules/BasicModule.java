package de.tostsoft.mpdclient.modules;


import de.tostsoft.mpdclient.MpdClient;

import java.util.ArrayList;

public abstract class BasicModule<T> {
    protected MpdClient player;
    protected ArrayList<T> listeners = new ArrayList<>();

    public BasicModule(MpdClient player){
        this.player = player;
    }

    public void addListener(T t){
        listeners.add(t);
    }

    public void removeListener(T t){
        listeners.remove(t);
    }

    public abstract void handleResult(PlayerCommandResult result);

    public void reset(){}
}
