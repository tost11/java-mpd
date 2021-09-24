package de.tostsoft.mpdclient;


import de.tostsoft.mpdclient.modules.*;
import de.tostsoft.mpdclient.modules.interfaces.BasicResultListener;
import de.tostsoft.mpdclient.tools.Logger;
import de.tostsoft.mpdclient.tools.Timer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class MpdClient {
    private String connectIp;
    private int connectPort;
    private Socket connectSocket;
    private int [] version = new int[3];

    private boolean byConnect = false;
    private Socket threadetConnectSocket = null;
    private Thread connectThread = null;
    private String threadetConnectIp;
    private int threadetConnectPort;
    private IOException connectException = null;

    private BufferedWriter sockerWriter;

    private String savedConnectIp;
    private int saveConnectPort;

    private ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
    private byte cTmpBuff[] = new byte[1];
    private boolean redSuccesfull = false;

    private boolean callListenersByInit = false;

    private ArrayList<DisconnectListener> disconnectListeners = new ArrayList<>();;
    private HashMap<String,BasicModule> modules = new HashMap<>();
    private LinkedList<PlayerCommandResult> querryOrder = new LinkedList();
    private Timer timer = new Timer(true);

    private ArrayList<BasicResultListener> basicResultListeners = new ArrayList<>();
    public void addListener(BasicResultListener t){
        basicResultListeners.add(t);
    }
    public void removeListener(BasicResultListener t){
        basicResultListeners.remove(t);
    }

    public MpdClient(){
        this("127.0.0.1",6600);
    }

    public MpdClient(String ip){
        this(ip,6600);
    }

    public MpdClient(String ip, int port){
        version[0]=0;
        version[1]=0;
        version[2]=0;
        connectIp = ip;
        connectPort = port;
        connectSocket = null;
        modules.put(OptionsModule.class.toString(),new OptionsModule(this));
        modules.put(PlayerModule.class.toString(),new PlayerModule(this));
        modules.put(PlaylistModule.class.toString(),new PlaylistModule(this));
        modules.put(PlaybackModule.class.toString(),new PlaybackModule(this));
        modules.put(StatModule.class.toString(),new StatModule(this));
        modules.put(DatabaseModule.class.toString(),new DatabaseModule(this));
        modules.put(CustomCommandModule.class.toString(),new CustomCommandModule(this));
        modules.put(CoverModule.class.toString(),new CoverModule(this));
        for(BasicModule it: modules.values()){
            it.reset();
        }
    }

    protected void finalize(){
        if(threadetConnectSocket != null){
            connectThread.interrupt();
        }
    }

    public boolean connect() {
        return connect(connectIp, connectPort,false);
    }

    public boolean connect(boolean asyncron) {
        return connect(connectIp, connectPort,asyncron);
    }

    private boolean connect(String ip, Integer port, boolean asyncron){
        if(ip == null || ip.isEmpty()){
            savedConnectIp = connectIp;
        }else{
            savedConnectIp = ip;
        }
        if(port == null || port <= 0){
            saveConnectPort = connectPort;
            port = connectPort;
        }else{
            saveConnectPort = port;
        }
        if(connectSocket != null || byConnect) {
            return false;//todo exeption
        }

        Logger.getInstance().log(Logger.Logtype.INFO,"Try to connect to MPD-Server: "+ip+":"+port);

        if(asyncron){
            byConnect = true;
            threadetConnectIp = ip;
            threadetConnectPort = port;
            Thread t = new Thread(()->{
                try {
                    Socket s = new Socket(threadetConnectIp, threadetConnectPort);
                    threadetConnectSocket = s;//variable is importetn for multithreading (i suppose XD)
                }catch (IOException ex){
                    connectException = ex;
                    Logger.getInstance().log(Logger.Logtype.INFO,"Could not connect to connect to MPD-Server");
                }
            });
            t.start();
            return true;
        }

        return reConnect(null);
    }


    private boolean reConnect(Socket socket){
        querryOrder.clear();
        try {
            if(socket == null) {
                connectSocket = new Socket(savedConnectIp, saveConnectPort);
            }else{
                connectSocket = socket;
            }
            connectSocket.setKeepAlive(true);

            sockerWriter = new BufferedWriter( new OutputStreamWriter(connectSocket.getOutputStream()));

            timer.resetCounter();

            while(!readline());
            String line = byteOutput.toString();
            if(!line.startsWith("OK MPD")){
                connectSocket.close();
                connectSocket = null;
                return false;
            }

            parseVersion(line.replace("OK MPD ",""));

            //playlist info first imporatta
            sockerWriter.write("playlistinfo\n");
            querryOrder.add(new PlayerCommandResult("playlistinfo"));
            sockerWriter.write("status\n");
            querryOrder.add(new PlayerCommandResult("status"));
            sockerWriter.write("stats\n");
            querryOrder.add(new PlayerCommandResult("stats"));
            for(DatabaseModule.EN_MusikDataType type : DatabaseModule.EN_MusikDataType.values()){
                DatabaseModule mod = getDatabase();
                if(mod.getIsMusikDataTypeActive(type)){
                    String q = mod.getQuerryByType(type)+"\n";
                    sockerWriter.write(q);
                    querryOrder.add(new PlayerCommandResult(q.trim()));
                }
            }
            sockerWriter.write("idle\n");
            querryOrder.add(new PlayerCommandResult("idle"));
            sockerWriter.flush();
            while(connectSocket.getInputStream().available()<0);
            update();
            Logger.getInstance().log(Logger.Logtype.DEBUG,"Connection established");
        }catch(IOException ex){
            Logger.getInstance().log(Logger.Logtype.DEBUG,"Could not connect to server: '\"+connectIp+\"'");
            connectSocket =null;
            return false;
        }
        return true;
    }

    private void parseVersion(final String versionLine){
        String[] vals = versionLine.split("\\.");
        for(int i=0;i<3;i++){
            version[i]=Integer.parseInt(vals[i]);
        }
    }

    private boolean tryReconnect(){
        Logger.getInstance().log(Logger.Logtype.DEBUG,"Try reconnect...");
        if(reConnect(null)){//lost connection
            update();
        }else{
            Logger.getInstance().log(Logger.Logtype.DEBUG,"Disconnected from server");
            disconnect();
            return false;
        }
        return true;
    }

    public void disconnect(){
        if(sockerWriter != null){//bit hacky but works
            callDisconnectListeners();
        }
        connectSocket = null;
        sockerWriter = null;
        querryOrder.clear();
        for(BasicModule it: modules.values()){
            it.reset();
        }
    }

    public boolean isConnected(){
        return connectSocket != null;
    }

    public boolean byConnect(){
        return byConnect;
    }

    public void update(){
        if(connectSocket == null){
            if(byConnect && threadetConnectSocket == null && connectException == null){//by connect but not finished
                return;
            }
            byConnect = false;
            if(connectException != null){
                //todo crate lisstener and throw event
                connectException = null;
                return;
            }
            boolean ret = reConnect(threadetConnectSocket);
            threadetConnectSocket = null;
            if(!ret){//handshake not succesfull
                return;
            }
        }
        timer.Update();
        if(timer.getCounterSeconds() > 1){//sendisAlive
            //System.out.println("Send keepalive");
            if(querry("")  < 0){
                return;//socket disconnected and reconect failed
            }
            timer.resetCounter();
        }
        try {
            while (readline()) {
                String line = byteOutput.toString();
                handleResult(line);
                //System.out.println(querryOrder.getFirst().getCommand());
                //System.out.println(line);
                if (querryOrder.getFirst().getCommand().startsWith("albumart ") && line.startsWith("binary: ")) {
                    int buffsize = Integer.parseInt(line.replace("binary: ", ""));

                    byte buff[] = new byte[buffsize];
                    connectSocket.getInputStream().read(buff, 0, buffsize);

                    String decoded = new String(buff, "ISO-8859-1");

                    handleResult(decoded);

                    byteOutput = new ByteArrayOutputStream();
                    //handleResult("OK");
                }
            }
            getPlayback().updateCurrentTime();
        }catch(IOException ex){
            tryReconnect();
        }
    }

    private boolean readline() throws IOException{
        if(redSuccesfull){
            byteOutput = new ByteArrayOutputStream();
        }
        while(true){
            if(!connectSocket.isConnected()){
                throw new IOException("Socket not connected");
            }
            if(connectSocket.getInputStream().available()<=0){
                return false;
            }
            connectSocket.getInputStream().read(cTmpBuff,0,1);
            if (cTmpBuff[0] == '\n')
                return redSuccesfull = true;
            byteOutput.write(cTmpBuff);
        }
    }

    public void addDisconnectListener(DisconnectListener lis){
        disconnectListeners.add(lis);
    }

    private void callDisconnectListeners(){
        for(DisconnectListener it: disconnectListeners){
            it.disconnected();
        }
    }

    private void handleResult(String line){
        if(line.startsWith("ACK")){
            Logger.getInstance().log(Logger.Logtype.WARNING,"Received errro from MPD: "+line);
        }
        if(line.startsWith("changed: ")){
            Logger.getInstance().log(Logger.Logtype.DEBUG,"Received change from MPD");
            String l = line.split(":",2)[1].trim();
            if(l.equals("update")){
                querry("status",false);
                querry("stats",true);
            }else if(l.equals("options") || l.equals("player") || l.equals("mixer")){
               querry("status");
            }else if(l.equals("stored_playlist")){
                querry("listplaylists");
            }else if(l.equals("playlist")){
                querry("playlistinfo");
            }else{
                querry("",false);
            }
        }else{
            PlayerCommandResult rem = querryOrder.getFirst();
            if(line.equals("OK") || line.startsWith("ACK")){
                //call listeners with last result
                for(BasicModule it: modules.values()) {
                    it.handleResult(rem);
                }
                basicResultListeners.forEach(lis->lis.call(rem));
                //remove and check next
                querryOrder.removeFirst();
                if(rem.equals("idle") && !querryOrder.isEmpty() && querryOrder.getFirst().equals("noidle")){
                    //System.out.println("Extra removed: noidle");
                    querryOrder.removeFirst();
                }
            }else {
                rem.addResult(line);
            }
        }
    }

    private int querry(String line, boolean addIdle){
        //System.out.println("querry done: "+line+" "+removeIdle);
        if(connectSocket == null){
            return -1;
        }
        //mutex.lock();
        PlayerCommandResult res = null;
        try {
            if(querryOrder.size() == 0 || querryOrder.getLast().getCommand().equals("idle")){
                querryOrder.addLast(new PlayerCommandResult("noidle"));
                sockerWriter.write("noidle\n");
            }
            /*if(removeIdle){
                querryOrder.addLast(new PlayerCommandResult("noidle"));
                sockerWriter.write("noidle\n");
            }*/
            if(!line.isEmpty()){
                querryOrder.addLast(res = new PlayerCommandResult(line));
                sockerWriter.write(line+"\n");
                Logger.getInstance().log(Logger.Logtype.DEBUG,"send "+line);
            }
            if(addIdle) {
                sockerWriter.write("idle\n");
                querryOrder.addLast(new PlayerCommandResult("idle"));
            }
            sockerWriter.flush();
        }catch(IOException ex){
            Logger.getInstance().log(Logger.Logtype.WARNING,"Lost connection to server because of: "+ex.getMessage());
            if(tryReconnect()){
                return querry(line,addIdle);
            }
            return -2;
        }
        return res == null ? 0 : res.getId();
    }

    public String getIp(){
        return connectIp;
    }

    public void setIp(String ip){
        connectIp = ip;
    }

    public int querry(String line){
        return querry(line,true);
    }

    private <T> T getModule(Class<T> t){
        return (T)(modules.get(t.toString()));
    }

    public OptionsModule getOptions(){
        return getModule(OptionsModule.class);
    }

    public StatModule getStats(){
        return getModule(StatModule.class);
    }

    public PlayerModule getPlayer(){
        return getModule(PlayerModule.class);
    }

    public PlaylistModule getPlaylist(){
        return getModule(PlaylistModule.class);
    }

    public PlaybackModule getPlayback(){
        return getModule(PlaybackModule.class);
    }

    public DatabaseModule getDatabase(){
        return getModule(DatabaseModule.class);
    }

    public CoverModule getCover(){
        return getModule(CoverModule.class);
    }

    public CustomCommandModule getCustomCommand(){
        return getModule(CustomCommandModule.class);
    }

    public void setCallListenerByInit(boolean val){
        callListenersByInit =val;
    }

    public boolean getCallLisntenersByInit(){
        return callListenersByInit;
    }

    public boolean isVersionAboveOrSame(int major,int minor,int patch){
        if(major == version[0]){
            if(minor == version[1]){
                return version[2] >= patch;
            }
            return version[1] > minor;
        }
        return version[0] > major;
    }

    static public String excapeQuerryString(final String value){
        return excapeQuerryString(value,false);
    }

    static public String excapeQuerryString(final String value,boolean extraescape){
        String val = value.replace("\\","\\\\").replace("/","\\/").replace("(","\\(").replace(")","\\)").replace(" ","\\ ").replace("\"","\\\"").replace("\'","\\\'");
        return extraescape ? val.replace("\\","\\\\") : val;
    }

    public String getMPDVersion(){
        if(!isConnected()){
            return null;
        }
        return ""+version[0]+"."+version[1]+"."+version[2];
    }
}
