package de.tostsoft.mpdclient;


import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.model.Cover;
import de.tostsoft.mpdclient.modules.interfaces.CoverListener;

import java.util.List;

import static java.lang.System.exit;

public class DownloadCover {


    static boolean running;

    //TODO test with MPD version 0.21.*
    public static void main(String[] args)throws InterruptedException{
        MpdClient mpdClient = new MpdClient();
        if(!mpdClient.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        running = true;
        Thread t = new Thread(()->{
            mpdClient.getCover().setScaleSize(100,100);//sets scaling of image (automaticly scales it for lower memmory use)
            mpdClient.getCover().CACHE_COVER = true;//saves cover in ram
            mpdClient.getCover().SAVE_COVER = false;//saves cover in folder
            CoverListener coverListener = new CoverListener() {
                @Override
                public void call(Cover cover) {
                    System.out.println("New Cover downloaded");
                    System.out.println("Uri: "+cover.getUri());
                    System.out.println("Filename: "+cover.getFilename());
                    //System.out.println("Bytes: "+cover.getImage());//byte stream to view wherever you whant
                }
            };
            mpdClient.getCover().addListener(coverListener);
            try {
                while (running) {
                    Thread.sleep(100);
                    mpdClient.update();
                }
            }catch(InterruptedException ex){}
            mpdClient.getCover().removeListener(coverListener);
        });

        t.start();

        Thread.sleep(1000);

        //TODO add / to playlist
        mpdClient.querry("clear");

        Thread.sleep(1000);
        List<String> rootFolders = mpdClient.getDatabase().getFiles();
        if(rootFolders.isEmpty()){
            System.out.println("Database is empty");
            running = false;
            t.join();
            mpdClient.disconnect();
            return;
        }

        Cover cover = mpdClient.getCover().getCover(rootFolders.get(0));//try to download cover from this folder
        if(cover != null){//Cover already in  chche -> coud not happen
            System.out.println("Cover is already here... but how?");
            running = false;
            t.join();
            mpdClient.disconnect();
            return;
        }

        //TODO return error whenn image not found

        Thread.sleep(5000);//longer time too wait because cover download could tage some time

        cover = mpdClient.getCover().getCover(rootFolders.get(0));//try to download cover from this folder
        if(cover == null){//cover not here maby there is no one or still in progress
            System.out.println("No Cover available or still by downloading");
        }else{
            System.out.println("Returned cached cover");
        }

        running = false;

        t.join();

        mpdClient.disconnect();

    }

}
