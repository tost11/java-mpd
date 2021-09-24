package de.tostsoft.mpdclient;

import de.tostsoft.mpdclient.MpdClient;

import static java.lang.System.exit;

public class CheckVersion {
    public static void  main(String args[]){
        MpdClient mpdClient = new MpdClient();
        if(!mpdClient.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        System.out.println("MPD-Server version is: "+mpdClient.getMPDVersion());
        mpdClient.disconnect();
    }
}
