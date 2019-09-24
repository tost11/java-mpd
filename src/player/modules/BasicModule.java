package player.modules;

import player.Player;

import java.util.ArrayList;

public abstract class BasicModule<T> {
    protected Player player;
    protected ArrayList<T> listeners = new ArrayList<>();

    public BasicModule(Player player){
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
