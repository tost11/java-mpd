package examples;

import player.Player;
import player.modules.PlaybackModule;
import player.modules.PlayerModule;
import player.modules.interfaces.PlaybackListener;
import player.modules.interfaces.PlayerListener;
import player.modules.interfaces.PlaylistListener;

import java.util.List;

import static java.lang.System.exit;

public class CheckPlayStatus {

    static boolean running;

    public static void main(String[] args)throws InterruptedException{
        Player player = new Player();
        if(!player.connect()){
            System.out.println("Could not connect to MPD-Server");
            exit(1);
        };
        running = true;
        Thread t = new Thread(()->{
            PlaylistListener playlistListener = new PlaylistListener() {
                @Override
                public void changed() {
                    System.out.println("New Playlist is:");
                    player.getPlaylist().getPlaylist().forEach(song->{
                        System.out.println("->"+song);
                    });
                }
            };
            player.getPlaylist().addListener(playlistListener);
            PlayerListener listener = new PlayerListener() {
                @Override
                public void changed(PlayerModule.PlayerStatus staus) {
                    System.out.println("Player change: "+staus);
                }
            };
            player.getPlayer().addListener(listener);
            try {
                while (running) {
                    Thread.sleep(100);
                    player.update();
                }
            }catch(InterruptedException ex){}
            player.getPlayer().removeListener(listener);
            player.getPlaylist().removeListener(playlistListener);
        });

        t.start();

        Thread.sleep(1000);

        //TODO add / to playlist
        player.Querry("clear");

        Thread.sleep(1000);
        List<String> rootFolders = player.getDatabase().getFiles();
        if(rootFolders.isEmpty()){
            System.out.println("Database is empty");
            return;
        }

        //mpd 0.21.* and above
        //player.Querry("searchadd \"(base '"+Player.excapeQuerryString("/",true)+"')\"");
        player.Querry("searchadd base "+Player.excapeQuerryString(rootFolders.get(0)));

        //Thread.sleep(2000);

        System.out.println("Set player status to play");
        player.getPlayer().setStatus(PlayerModule.PlayerStatus.PLAY);

        Thread.sleep(2000);

        System.out.println("Set player status to stop");
        player.getPlayer().setStatus(PlayerModule.PlayerStatus.STOP);

        Thread.sleep(2000);

        running = false;

        t.join();

        player.disconnect();

    }
}
