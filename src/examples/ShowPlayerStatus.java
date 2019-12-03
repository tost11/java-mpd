package examples;

import player.Player;
import player.modules.PlayerModule;

import static java.lang.System.exit;
import static java.lang.System.setOut;

public class ShowPlayerStatus {
    public static void  main(String args[]){
        Player player = new Player();
        if(!player.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        player.getPlayer().addListener(status->{
            System.out.println("Player changed to status: "+status);
        });
        while(player.isConnected()){
            player.update();
        }
        player.disconnect();
    }
}
