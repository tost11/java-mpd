package player.modules;

import player.model.PlaylistSong;
import player.modules.interfaces.PlaylistListener;
import player.Player;
import player.Tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class PlaylistModule extends BasicModule<PlaylistListener> {

    private HashMap<Integer,PlaylistSong> songs = new HashMap<>();
    private ArrayList<PlaylistSong> playlist = new ArrayList<>();

    public PlaylistModule(Player player) {
        super(player);
    }

    @Override
    public void handleResult(PlayerCommandResult result) {
        if(result.getCommand().equals("playlistinfo")){
            songs.clear();
            playlist.clear();
            PlaylistSong tmpSong = new PlaylistSong();
            for(String line:result.getResults()) {
                if(line.startsWith("Title: ")){
                    tmpSong.name = Tools.seperateString(line);
                }else if(line.startsWith("Pos: ")){
                    tmpSong.position = Tools.seperateInt(line);
                }else if(line.startsWith("Album: ")){
                    tmpSong.album = Tools.seperateString(line);
                }else if(line.startsWith("Artist: ")){
                    tmpSong.artist = Tools.seperateString(line);
                }else if(line.startsWith("Genre: ")){
                    tmpSong.genre = Tools.seperateString(line);
                }else if(line.startsWith("file: ")){
                    tmpSong.filename = Tools.seperateString(line);
                }else if(line.startsWith("Time: ")){
                    tmpSong.time = Tools.seperateInt(line);
                }else if(line.startsWith("Id: ")) {
                    int id = Tools.seperateInt(line);
                    tmpSong.id = id;
                    songs.put(id,tmpSong);
                    playlist.add(tmpSong);
                    tmpSong = new PlaylistSong();
                }
            }
            callListaners();
        }
    }

    public PlaylistSong getSong(int id){
        return songs.get(id);
    }

    public void callListaners(){
        for(PlaylistListener it: listeners){
            it.changed();
        }
    }

    public Collection<PlaylistSong> getPlaylist(){
        return playlist;
    }
}
