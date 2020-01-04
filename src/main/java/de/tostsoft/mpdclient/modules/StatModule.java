package de.tostsoft.mpdclient.modules;


import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.modules.interfaces.StatListener;

public class StatModule extends BasicModule<StatListener> {
    public enum StatStatus{
        ARTISTS,
        ALBUMS,
        SONGS
    }

    private Integer stats[] = new Integer[StatStatus.values().length];

    public StatModule(MpdClient player) {
        super(player);
    }

    @Override
    public void handleResult(PlayerCommandResult result) {
        if(result.getCommand().equals("stats")){
            for(String line:result.getResults()) {
                for (int i = 0; i < StatStatus.values().length; i++) {
                    if (line.startsWith(StatStatus.values()[i].toString().toLowerCase())) {
                        int val = Integer.parseInt(line.split(":")[1].trim());
                        if (stats[i] == null) {
                            stats[i] = val;
                            if (player.getCallLisntenersByInit()) {
                                callListeners(StatStatus.values()[i], val);
                            }
                        } else {
                            if (stats[i] != val) {
                                stats[i] = val;
                                callListeners(StatStatus.values()[i], val);
                            }
                        }
                    }
                }
            }
        }
    }

    private void callListeners(StatStatus stat, int val){
        for(StatListener lis: listeners){
            lis.changed(stat,val);
        }
    }


    public Integer getStatus(StatModule.StatStatus stats){
        return this.stats[stats.ordinal()];
    }

    public Integer getNumberSongs(){
        return stats[StatStatus.SONGS.ordinal()];
    }
}
