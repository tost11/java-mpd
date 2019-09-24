package examples;

import player.Player;

import static java.lang.System.exit;

public class CheckVersion {
    public static void  main(String args[]){
        Player player = new Player();
        if(!player.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        System.out.println("MPD-Server version is: "+player.getMPDVersion());
        player.disconnect();
    }
}
