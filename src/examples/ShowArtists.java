package examples;

import player.Player;
import player.modules.DatabaseModule;

import static java.lang.System.console;
import static java.lang.System.exit;

public class ShowArtists {
    public static void  main(String args[]) throws InterruptedException{
        Player player = new Player();
        if(!player.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        for(int i=0;i<100;i++){
            Thread.sleep(100);
            player.update();
        }

        for(String artist:player.getDatabase().getArtists()){
            System.out.println(artist);
        }
        player.disconnect();
    }
}
