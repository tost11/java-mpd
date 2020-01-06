package de.tostsoft.mpdclient;


import de.tostsoft.mpdclient.tools.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Tools {
    public static String configFileName = "config.properties";

    public static int seperateInt(String line){
        return Integer.parseInt(line.split(":")[1].trim());
    }

    public static String seperateString(String line){
        return line.split(":",2)[1].trim();
    }

    public static float seperateFloat(String line){
        return Float.parseFloat(line.split(":")[1].trim());
    }

    public static String formatTime(int time){
        int num = time%60;
        return ""+time/60+":"+(num<10?"0":"")+time%60;
    }

    static public String loadFromProperties(final String name){//load given variable from config file or returns null
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(configFileName));
            String st = prop.getProperty(name);
            if(st != null && !st.isEmpty()){
                return st;
            }
        }catch(IOException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not load config file: "+ex.getMessage());
        }
        return null;
    }


    static public Integer loadFromPropertiesInt(final String name){//load given variable from config file or returns null
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(configFileName));
            String st = prop.getProperty(name);
            if(st != null && !st.isEmpty()){
                return Integer.parseInt(st);
            }
        }catch(IOException | NumberFormatException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not load '"+ name + "' from config file: "+ex.getMessage());
        }
        return null;
    }

    static public Boolean loadFromPropertiesBoolean(final String name){//load given variable from config file or returns null
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(configFileName));
            String st = prop.getProperty(name);
            if(st != null && !st.isEmpty()){
                return Boolean.parseBoolean(st);
            }
        }catch(IOException | NumberFormatException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not load '"+ name + "' from config file: "+ex.getMessage());
        }
        return null;
    }

    static public Vector<String> loadFromPropertiesIncreasing(final String name){//load given variable from config file or returns null
        Vector<String> vec = new Vector<>();
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(configFileName));
            for(int i=0;i<100;i++){
                String st = prop.getProperty(name+i);
                if(st != null && !st.isEmpty()){
                    vec.add(st);
                }
            }
            return vec;
        }catch(IOException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not load config file: "+ex.getMessage());
        }
        return new Vector<String>();
    }

    static public long getFoldersize(String name){
        File folder = new File(name);
        if(!folder.isDirectory()) {
            return -1;
        }
        return getFolderSizeRec(folder);
    }

    static public List<String> getFilesByExtention(final String path,String extentions){
        return getFilesByExtention(path,Arrays.asList(extentions));
    }

    static public List<String> getFilesByExtention(final String path,final Collection<String> extentions){
        List<String> list = new ArrayList<>();
        File folder = new File(path);
        File[] filelist = folder.listFiles();
        if(filelist == null){//is not a directory
            return list;
        }
        for(int i=0;i<filelist.length;i++){
            File f = filelist[i];
            if(!f.isFile()){
                continue;
            }
            if(extentions.stream().noneMatch(ex->f.getName().endsWith(ex))){
                continue;
            }
            list.add(f.getName());
        }
        return list;
    }

    static public long getFileSize(String pathToFile){
        File f = new File(pathToFile);
        if(!f.exists() || f.isDirectory()){
            return 0;
        }
        return f.length();
    }

    static private long getFolderSizeRec(File folder){
        long size = 0;
        File[] filelist = folder.listFiles();
        for(int i=0;i<filelist.length;i++){
            File f = filelist[i];
            if(f.isDirectory()){
                size+=getFolderSizeRec(f);
            }else{
                size+=f.length();
            }
        }
        return size;
    }

    static public boolean compareStringArray(List<String> l1,List<String> l2){
        if(l1.size() != l2.size()){
            return false;
        }
        for(int i=0;i<l1.size();i++){
            if(!l1.get(i).equals(l2.get(i))){
                return false;
            }
        }
        return true;
    }

    static public boolean StringToBoolean(final String s){
        String tmp = s.toLowerCase();
        return tmp.equals("true") || tmp.equals("wahr") || tmp.equals("on");
    }

}
