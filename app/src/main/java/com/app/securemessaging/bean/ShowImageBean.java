package com.app.securemessaging.bean;

import java.io.Serializable;

/**
 * Show image bean class to set the image and type.
 */

public class ShowImageBean implements Serializable{
    private String image;
    private String type ;
    private long fileSize ;
    private String thumbnail ;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
