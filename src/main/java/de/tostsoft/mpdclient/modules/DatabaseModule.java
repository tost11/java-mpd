package de.tostsoft.mpdclient.modules;


import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.modules.interfaces.DatabaseListener;

import java.util.*;

public class DatabaseModule extends BasicModule<DatabaseListener> {
    public enum EN_MusikDataType{
        FILE,
        ALBUM,
        ARTIST,
        PLAYLIST,
        GENRE,
        SONG;

        @Override
        public String toString() {
            if(this==FILE){
                return "Datein";
            }else if(this==ALBUM){
                return "Alben";
            }else if(this==ARTIST){
                return "Interpreten";
            }else if(this==PLAYLIST){
                return "Wiedergabelisten";
            }else if(this==GENRE){
                return "Genres";
            }else if(this==SONG){
                return "Songs";
            }
            return null;
        }
    };

    private ArrayList<String>[] data = new ArrayList[EN_MusikDataType.values().length];
    private Boolean[] activeTypes = new Boolean[EN_MusikDataType.values().length];//beacuse big musik database brake querrys -> use custom querry
    private HashSet<String> isDirectory = new HashSet<String>();

    public DatabaseModule(MpdClient player) {
        super(player);
        for(int i = 0;i<EN_MusikDataType.values().length;i++){
            data[i] = new ArrayList<>();
            String s = Tools.loadFromProperties("Databaserequest_"+EN_MusikDataType.values()[i]);
            if(s != null){
                activeTypes[i] = Tools.StringToBoolean(s);
            }else{
                activeTypes[i] = EN_MusikDataType.values()[i] != EN_MusikDataType.SONG;
            }
        }
    }

    private void setMusikDataTypeStatus(EN_MusikDataType type, boolean val){
        if(activeTypes[type.ordinal()] != val){
            if(val){
                player.querry(getQuerryByType(type));
            }else{
                data[type.ordinal()].clear();
                callListaners(type);
            }
            activeTypes[type.ordinal()] = val;
        }

    }

    public String getQuerryByType(EN_MusikDataType type){
        if(type == EN_MusikDataType.PLAYLIST){
            return "listplaylists";
        }else if(type == EN_MusikDataType.ALBUM){
            return "list album";
        }else if(type == EN_MusikDataType.ARTIST){
            return "list artist";
        }else if(type == EN_MusikDataType.GENRE){
            return "list genre";
        }else if(type == EN_MusikDataType.SONG){
            return "list title";
        }else if(type == EN_MusikDataType.FILE){
            return "lsinfo";
        }
        throw new RuntimeException("DatabaseMoudle:getQuerryByType -> no valid type");
    }

    private void callListaners(EN_MusikDataType reason){
        for(DatabaseListener it: listeners){
            it.chagned(reason);
        }
    }

    public boolean getIsMusikDataTypeActive(EN_MusikDataType type){
        return activeTypes[type.ordinal()];
    }

    @Override
    public void handleResult(PlayerCommandResult result){
        if(result.getCommand().equals(getQuerryByType(EN_MusikDataType.PLAYLIST))){
            checkData(EN_MusikDataType.PLAYLIST, Collections.singletonList("playlist: "),result.getResults());
        }else if(result.getCommand().equals(getQuerryByType(EN_MusikDataType.ARTIST))){
            checkData(EN_MusikDataType.ARTIST,Collections.singletonList("Artist: "),result.getResults());
        }else if(result.getCommand().equals(getQuerryByType(EN_MusikDataType.ALBUM))){
            checkData(EN_MusikDataType.ALBUM,Collections.singletonList("Album: "),result.getResults());
        }else if(result.getCommand().equals(getQuerryByType(EN_MusikDataType.GENRE))){
            checkData(EN_MusikDataType.GENRE, Collections.singletonList("Genre: "),result.getResults());
        }else if(result.getCommand().equals(getQuerryByType(EN_MusikDataType.FILE))){
            checkDirectorysAndFolders(result.getResults());
            checkData(EN_MusikDataType.FILE, Arrays.asList("directory: ", "file: "),result.getResults());
        }
    }

    private void checkData(EN_MusikDataType type, Collection<String> lookFors, ArrayList<String> result){
        if(!activeTypes[type.ordinal()]){
            //not use this because it is a querry witch is late because status already changed
            return;
        }
        ArrayList<String> tmp = new ArrayList<>();
        for(String lookFor:lookFors) {
            ArrayList<String> tmp2 = new ArrayList<>();
            for (String line : result) {
                if (line.startsWith(lookFor)) {
                    tmp.add(line.split(":",2)[1].trim());
                }
            }
            tmp2.sort(String::compareTo);
            tmp.addAll(tmp2);
        }
        if(!Tools.compareStringArray(tmp, data[type.ordinal()])){
            data[type.ordinal()] = tmp;
            callListaners(type);
        }
    }

    private void checkDirectorysAndFolders(ArrayList<String> result){
        isDirectory.clear();
        for(String line:result){
            if(line.startsWith("directory: ")){
                isDirectory.add(line.split(":",2)[1].trim());
            }
        }
    }

    public ArrayList<String> getPlaylists(){
        return data[EN_MusikDataType.PLAYLIST.ordinal()];
    }

    public ArrayList<String> getArtists(){
        return data[EN_MusikDataType.ARTIST.ordinal()];
    }

    public ArrayList<String> getFiles(){
        return data[EN_MusikDataType.FILE.ordinal()];
    }

    public ArrayList<String> getData(EN_MusikDataType type){
        return data[type.ordinal()];//returns emtpy arraylist
    }

    public boolean isDirectory(final String value){
        return isDirectory.contains(value);
    }
}
