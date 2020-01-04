package de.tostsoft.mpdclient.examples;


import de.tostsoft.mpdclient.MpdClient;

import static java.lang.System.console;
import static java.lang.System.exit;

public class ShowArtists {
    public static void  main(String args[]) throws InterruptedException{
        MpdClient mpdClient = new MpdClient();
        if(!mpdClient.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        for(int i=0;i<100;i++){
            Thread.sleep(100);
            mpdClient.update();
        }

        for(String artist:mpdClient.getDatabase().getArtists()){
            System.out.println(artist);
        }
        mpdClient.disconnect();
    }
}
