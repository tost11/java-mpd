package player.modules;

import player.model.Cover;
import player.modules.interfaces.CoverListener;
import player.Player;
import player.tools.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class CoverModule extends BasicModule<CoverListener> {
    public class CoverData{
        String uri;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        public CoverData(String uri){
            this.uri = uri;
        }
    }

    public class ScaleSize{
        public ScaleSize(int x,int y){
            this.x = x;
            this.y = y;
        }
        public int x;
        public int y;
    }

    private HashMap<String,CoverData> loadingCovers = new HashMap<>();
    private HashMap<String, Cover> covers = new HashMap<>();
    private static final String COVERPATH = "covers";
    public static boolean CACHE_COVER = true;
    public static boolean SAVE_COVER = true;
    private static String SAVE_COVER_FORMAT = "jpg";
    public ScaleSize SCALE_COVER = new ScaleSize(200,200);

    public CoverModule(Player player) {
        super(player);
    }

    @Override
    public void handleResult(PlayerCommandResult result) {
        if(result.getCommand().startsWith("albumart")){
            Logger.getInstance().log(Logger.Logtype.DEBUG,"Coverslice Received");

            List<String> res = result.getResults();


            String[] command_subs = result.getCommand().split("\"");
            for(int i=0;i<command_subs.length;i++){
                command_subs[i]= command_subs[i].trim();
            }
            if(res.size() < 3){//was not valid albumart command
                return;
            }

            CoverData coverData = loadingCovers.get(command_subs[1]);
            if(coverData == null){
                coverData = new CoverData(command_subs[1]);
                loadingCovers.put(coverData.uri,coverData);
            }

            int max = Integer.parseInt(res.get(0).replace("size: ",""));
            int red = Integer.parseInt(res.get(1).replace("binary: ",""));
            int off = Integer.parseInt(command_subs[2]);

            try {
                byte[] encoded = res.get(2).getBytes("ISO-8859-1");
                coverData.bytes.write(encoded);
                if(coverData.bytes.size() < max){//ask for next part
                    player.Querry("albumart \""+coverData.uri+"\" "+(off+red));
                    Logger.getInstance().log(Logger.Logtype.DEBUG,"Received: "+coverData.bytes.size()+"/"+max+" from '"+coverData.uri+"'");
                    return;
                }
                Logger.getInstance().log(Logger.Logtype.INFO,"Fully received cover wiht uri: "+coverData.uri);
                loadingCovers.remove(coverData.uri);

                /*File targetFile = new File("cover.png");
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(coverData.bytes.toByteArray());*/

                BufferedImage bImage = ImageIO.read(new ByteArrayInputStream(coverData.bytes.toByteArray(),0,coverData.bytes.size()));
                if(bImage == null){
                    Logger.getInstance().log(Logger.Logtype.WARNING,"Could not use cover unsuported format");
                    return;
                }

                Cover cover = new Cover(coverData.uri,bImage);

                if(SAVE_COVER) {
                    File path = new File(COVERPATH);
                    if(!path.exists()){
                        Files.createDirectories(Paths.get(COVERPATH));
                    }
                    String filename = escapePath(coverData.uri);
                    String fullFileName = COVERPATH + "/" + filename+"."+SAVE_COVER_FORMAT;
                    File file = new File(fullFileName);

                    if(SCALE_COVER != null){
                        bImage = resizeImage(bImage,(int)SCALE_COVER.x,(int)SCALE_COVER.y);
                    }

                    ImageIO.write(bImage ,SAVE_COVER_FORMAT,file);

                    /*while(!file.renameTo(new File("covers/test"))){
                        System.out.println("test");
                    }
                    file = new File("covers/test");
                    file.renameTo(new File(fullFileName));*/

                    cover.setFilename(fullFileName);
                }
                if(CACHE_COVER){
                    covers.put(cover.getUri(),cover);
                }
                callListeners(cover);
            }catch(IOException ex){
                Logger.getInstance().log(Logger.Logtype.ERROR,"Could not handle Cover result: "+ex.getMessage());
            }
        }
    }

    private void callListeners(Cover cover){
        for(CoverListener it: listeners){
            it.call(cover);
        }
    }

    private String escapePath(String path){
        String ret = path.replace("_","__");
        ret = ret.replace("/","_-");
        return ret;
    }

    public Cover getCover(String uri){
        return getCover(uri,true);
    }

    public Cover getCover(String uri, boolean request){
        if(CACHE_COVER){
            Cover cover = covers.get(uri);
            if(cover != null){
                return cover;
            }
        }
        if(SAVE_COVER) {
            String path = COVERPATH + "/" + escapePath(uri)+"."+SAVE_COVER_FORMAT;
            if (Files.exists(Paths.get(path))) {
                Cover cover = new Cover(uri,path);
                Logger.getInstance().log(Logger.Logtype.DEBUG,"Loadet cover from localfiles");
                if(CACHE_COVER){
                    try {
                        cover.setImage(ImageIO.read(new File(path)));
                        covers.put(cover.getUri(),cover);
                    }catch (IOException ex){
                        Logger.getInstance().log(Logger.Logtype.ERROR,"Error by loading Cover from Cache: "+ex.getMessage());
                    }
                }
                return cover;
            }else{
                Logger.getInstance().log(Logger.Logtype.DEBUG,"Could not load cover from file: "+path);
            }
        }

        if(request) {
            if (player.isVersionAboveOrSame(0,21,0)) {
                if (!loadingCovers.containsKey(uri)) {
                    loadingCovers.put(uri,new CoverData(uri));//TODO check if not succesfull and remove
                    player.Querry("albumart \"" + uri + "\" 0");
                }
            } else {
                Logger.getInstance().log(Logger.Logtype.WARNING,"Albumart is not supported in MPD version from server. Please update if you whant to use this feature");
            }
        }
        return null;
    }

    @Override
    public void reset() {
        loadingCovers.clear();
        covers.clear();
    }

    private BufferedImage resizeImage(BufferedImage src, int w, int h)
    {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int x, y;
        int ww = src.getWidth();
        int hh = src.getHeight();
        int[] ys = new int[h];
        for (y = 0; y < h; y++)
            ys[y] = y * hh / h;
        for (x = 0; x < w; x++) {
            int newX = x * ww / w;
            for (y = 0; y < h; y++) {
                int col = src.getRGB(newX, ys[y]);
                img.setRGB(x, y, col);
            }
        }
        return img;
    }
}
