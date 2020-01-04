package de.tostsoft.mpdclient.model;

import java.awt.image.BufferedImage;

public class Cover {
    String uri;
    String filename;
    BufferedImage image;

    public Cover(String uri,String filename){
        this.uri = uri;
        this.filename = filename;
        this.image = null;
    }

    public Cover(String uri,BufferedImage image){
        this.uri = uri;
        this.filename = null;
        this.image = image;
    }

    public void setImage(BufferedImage image){this.image = image;}

    public String getFilename() {
        return filename;
    }

    public String getUri() {
        return uri;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setFilename(String filename){
        this.filename = filename;
    }
}
