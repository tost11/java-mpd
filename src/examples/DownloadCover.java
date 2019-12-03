package examples;

import player.Player;
import player.model.Cover;
import player.modules.CoverModule;
import player.modules.PlayerModule;
import player.modules.interfaces.CoverListener;
import player.modules.interfaces.PlayerListener;
import player.modules.interfaces.PlaylistListener;

import java.util.List;

import static java.lang.System.exit;
import static java.lang.System.setOut;

public class DownloadCover {


    static boolean running;

    //TODO test with MPD version 0.21.*
    public static void main(String[] args)throws InterruptedException{
        Player player = new Player();
        if(!player.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        running = true;
        Thread t = new Thread(()->{
            player.getCover().setScaleSize(100,100);//sets scaling of image (automaticly scales it for lower memmory use)
            player.getCover().CACHE_COVER = true;//saves cover in ram
            player.getCover().SAVE_COVER = false;//saves cover in folder
            CoverListener coverListener = new CoverListener() {
                @Override
                public void call(Cover cover) {
                    System.out.println("New Cover downloaded");
                    System.out.println("Uri: "+cover.getUri());
                    System.out.println("Filename: "+cover.getFilename());
                    //System.out.println("Bytes: "+cover.getImage());//byte stream to view wherever you whant
                }
            };
            player.getCover().addListener(coverListener);
            try {
                while (running) {
                    Thread.sleep(100);
                    player.update();
                }
            }catch(InterruptedException ex){}
            player.getCover().removeListener(coverListener);
        });

        t.start();

        Thread.sleep(1000);

        //TODO add / to playlist
        player.Querry("clear");

        Thread.sleep(1000);
        List<String> rootFolders = player.getDatabase().getFiles();
        if(rootFolders.isEmpty()){
            System.out.println("Database is empty");
            running = false;
            t.join();
            player.disconnect();
            return;
        }

        Cover cover = player.getCover().getCover(rootFolders.get(0));//try to download cover from this folder
        if(cover != null){//Cover already in  chche -> coud not happen
            System.out.println("Cover is already here... but how?");
            running = false;
            t.join();
            player.disconnect();
            return;
        }

        //TODO return error whenn image not found

        Thread.sleep(5000);//longer time too wait because cover download could tage some time

        cover = player.getCover().getCover(rootFolders.get(0));//try to download cover from this folder
        if(cover == null){//cover not here maby there is no one or still in progress
            System.out.println("No Cover available or still by downloading");
        }else{
            System.out.println("Returned cached cover");
        }

        running = false;

        t.join();

        player.disconnect();

    }

}
