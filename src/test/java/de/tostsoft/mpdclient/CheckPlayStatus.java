package de.tostsoft.mpdclient;

import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.modules.PlayerModule;
import de.tostsoft.mpdclient.modules.interfaces.PlayerListener;
import de.tostsoft.mpdclient.modules.interfaces.PlaylistListener;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.lang.System.exit;

public class CheckPlayStatus {

    static boolean running;

    @Test
    public void CheckPlaylistsTest() throws Exception{
        MpdClient mpdClient = new MpdClient();
        if(!mpdClient.connect()){
            throw new RuntimeException("Could not connect to MPD-Server");
        };
        running = true;
        Thread t = new Thread(()->{
            PlaylistListener playlistListener = new PlaylistListener() {
                @Override
                public void changed() {
                    System.out.println("New Playlist is:");
                    mpdClient.getPlaylist().getPlaylist().forEach(song->{
                        System.out.println("->"+song);
                    });
                }
            };
            mpdClient.getPlaylist().addListener(playlistListener);
            PlayerListener listener = new PlayerListener() {
                @Override
                public void changed(PlayerModule.PlayerStatus staus) {
                    System.out.println("Player change: "+staus);
                }
            };
            mpdClient.getPlayer().addListener(listener);
            try {
                while (running) {
                    Thread.sleep(100);
                    mpdClient.update();
                }
            }catch(InterruptedException ex){}
            mpdClient.getPlayer().removeListener(listener);
            mpdClient.getPlaylist().removeListener(playlistListener);
        });

        t.start();

        Thread.sleep(1000);

        //TODO add / to playlist
        mpdClient.querry("clear");

        Thread.sleep(1000);
        List<String> rootFolders = mpdClient.getDatabase().getFiles();
        if(rootFolders.isEmpty()){
            throw new RuntimeException("Database is empty");
        }

        //mpd 0.21.* and above
        //player.Querry("searchadd \"(base '"+Player.excapeQuerryString("/",true)+"')\"");
        mpdClient.querry("searchadd base "+MpdClient.excapeQuerryString(rootFolders.get(0)));

        //Thread.sleep(2000);

        System.out.println("Set player status to play");
        mpdClient.getPlayer().setStatus(PlayerModule.PlayerStatus.PLAY);

        Thread.sleep(2000);

        System.out.println("Set player status to stop");
        mpdClient.getPlayer().setStatus(PlayerModule.PlayerStatus.STOP);

        Thread.sleep(2000);

        running = false;

        t.join();

        mpdClient.disconnect();

    }
}
