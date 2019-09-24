package player.modules;

import player.modules.interfaces.PlayerListener;
import player.Player;
import player.Tools;

public class PlayerModule extends BasicModule<PlayerListener> {
    public enum PlayerStatus{
        PLAY,
        STOP,
        PAUSE
    }

    private PlayerStatus playerStatus;

    public PlayerModule(Player player){
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
        player.Querry(status.toString().toLowerCase());
    }

    public void playNext(){
        player.Querry("next");
    }

    public void playPrevious(){
        player.Querry("previous");
    }

    boolean isPlaying(){
        return playerStatus == PlayerStatus.PLAY;
    }

    @Override
    public void reset() {
        playerStatus = null;
    }
}
