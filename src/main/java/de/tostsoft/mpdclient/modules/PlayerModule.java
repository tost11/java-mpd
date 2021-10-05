package de.tostsoft.mpdclient.modules;


import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.modules.interfaces.PlayerListener;

public class PlayerModule extends BasicModule<PlayerListener> {
    public enum PlayerStatus{
        PLAY,
        STOP,
        PAUSE
    }

    private PlayerStatus playerStatus;

    public PlayerModule(MpdClient player){
        super(player);
    }

    @Override
    public void handleResult(PlayerCommandResult result) {
        if(result.getCommand().equals("status")){
            for(String line:result.getResults()) {
                if (line.startsWith("state")) {
                    PlayerStatus status = PlayerStatus.valueOf(Tools.seperateString(line).toUpperCase());
                    if (playerStatus == null) {
                        playerStatus = status;
                        if (player.getCallLisntenersByInit()) {
                            callListners(status);
                        }
                    } else {
                        if (playerStatus != status) {
                            playerStatus = status;
                            callListners(status);
                        }
                    }
                }
            }
        }
    }

    private void callListners(PlayerStatus status){
        for(PlayerListener it: listeners){
            it.changed(status);
        }
    }

    public PlayerStatus getStatus() {
        return playerStatus;
    }

    public void setStatus(PlayerStatus status){
        player.querry(status.toString().toLowerCase());
    }

    public PlayerCommandResult playNext(){
        return player.querry("next");
    }

    public void playPrevious(){
        player.querry("previous");
    }

    boolean isPlaying(){
        return playerStatus == PlayerStatus.PLAY;
    }

    @Override
    public void reset() {
        playerStatus = null;
    }
}
