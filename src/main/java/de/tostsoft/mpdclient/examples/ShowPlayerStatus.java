package de.tostsoft.mpdclient.examples;

import de.tostsoft.mpdclient.MpdClient;

import static java.lang.System.exit;

public class ShowPlayerStatus {
    public static void  main(String args[]){
        MpdClient mpdClient = new MpdClient();
        if(!mpdClient.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        mpdClient.getPlayer().addListener(status->{
            System.out.println("Player changed to status: "+status);
        });
        while(mpdClient.isConnected()){
            mpdClient.update();
        }
        mpdClient.disconnect();
    }
}
