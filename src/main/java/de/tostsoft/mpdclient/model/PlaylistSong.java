package de.tostsoft.mpdclient.model;

public class PlaylistSong {
    public String name = "";
    public int position = 0;
    public int id = -1;
    public String artist ="";
    public String album ="";
    public String filename ="";
    public String genre ="";
    public int time;

    public PlaylistSong(String name, int id,int pos){
        this.name = name;
        position = pos;
        this.id = id;
    }

    public PlaylistSong(){
    }

    public String getName(){
        if(name.trim().isEmpty()){

            String file = filename;
            int index = file.lastIndexOf(".");
            if(index > -1){
                file = file.substring(0,index-1);
            }
            index = file.lastIndexOf("/");
            if(index > -1){
                file = file.substring(index+1,file.length());
            }
            return file;
        }else{
            return name;
        }
    }

    public String getCoverPath(){
        if(filename.isEmpty()){
            return null;
        }
        int index = filename.lastIndexOf("/");
        if(index == -1){
            return null;
        }
        return filename.substring(0,index);
    }
}
