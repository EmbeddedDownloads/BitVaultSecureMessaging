package com.app.securemessaging.bean;

public class SelectFilesBean  {

    private String name ;
    private String size ;
    private boolean isSelected = false ;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
    public  SelectFilesBean(){

    }

    public SelectFilesBean(String name, String size) {
        this.size = size;
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
