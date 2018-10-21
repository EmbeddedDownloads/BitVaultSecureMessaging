package com.app.securemessaging.bean;

import android.databinding.BaseObservable;

/**
 * Set filepath in create message bean.
 */

public class CreateMessageBean extends BaseObservable {
    private String filepath;


    public String getFilepath() {

        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }



    public CreateMessageBean(String filepath) {


        this.filepath = filepath;
    }
}
