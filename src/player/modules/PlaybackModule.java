package player.modules;

import player.model.PlaylistSong;
import player.modules.interfaces.PlaybackListener;
import player.Player;
import player.Tools;
import player.tools.Timer;

public class PlaybackModule extends BasicModule<PlaybackListener>{

    public enum PlaybackEvent{
        SONG_CHANGED,
        NEXTSONG_CHANGED,
        SONGPOSITION_CHANGED,
    }

    private Integer currentSong;
    private Integer nextSong;

    private Timer timer;

    public PlaybackModule(Player player){
        super(player);
        timer = new Timer();
    }

    @Override
    public void handleResult(PlayerCommandResult result){
        if(result.getCommand().equals("status")){
            for(String line:result.getResults()) {
                if (line.startsWith("songid")) {
                    int id = Tools.seperateInt(line);
                    if (currentSong == null) {
                        currentSong = id;
                    } else if (currentSong != id) {
                        currentSong = Tools.seperateInt(line);
                        callListeners(PlaybackEvent.SONG_CHANGED, getCurrentSong());
                    }
                } else if (line.startsWith("nextsong")) {
                    int id = Tools.seperateInt(line);
                    if (nextSong == null) {
                        nextSong = id;
                    } else if (nextSong != id) {
                        nextSong = Tools.seperateInt(line);
                        callListeners(PlaybackEvent.NEXTSONG_CHANGED, getNextSong());
                    }
                } else if (line.startsWith("elapsed")) {
                    Float f = Tools.seperateFloat(line);
                    timer.updateAndStartCounterS((double) f);
                    callListeners(PlaybackEvent.SONGPOSITION_CHANGED, f);
                } else if (line.startsWith("state: ")) {
                    if (Tools.seperateString(line).equals("stop")) {
                        timer.resetCounter();
                    }
                }
            }
        }
    }

    private void callListeners(PlaybackEvent ev,Object obj){
        for(PlaybackListener it: listeners){
            it.changed(ev,obj);
        }
    }

    public PlaylistSong getCurrentSong(){
        if(currentSong == null){
            return null;
        }
        return player.getPlaylist().getSong(currentSong);
    }

    public PlaylistSong getNextSong(){
        if(nextSong == null){
            return null;
        }
        return player.getPlaylist().getSong(nextSong);
    }

    public void updateCurrentTime(){
        if(player.getPlayer().isPlaying()){
            timer.Update();
            Float val=(float) timer.getCounterSeconds();
            if(getCurrentSong() != null) {
                int songlength = getCurrentSong().time;
                if (val > songlength) {
                    val = (float) songlength;
                }
                callListeners(PlaybackEvent.SONGPOSITION_CHANGED, val);
            }
        }
    }

    public void setSongPosition(float pos){
        player.Querry("seekcur "+pos);
    }

    public int getSongLength(){
        if(currentSong == null){
            return 0;
        }
        return getCurrentSong().time;
    }

    public float getSongPosition(){
        return (float) timer.getCounterSeconds();
    }
}
