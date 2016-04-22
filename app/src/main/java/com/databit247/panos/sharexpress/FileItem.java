package com.databit247.panos.sharexpress;


public class FileItem implements Comparable<FileItem> {
    private String name;
    private String data;
    private String date;
    private String path;
    private String image;

    public FileItem(String n, String d, String dt, String p, String img) {
        setName(n);
        setData(d);
        date = dt;
        path = p;
        image = img;
    }

    public FileItem() {

    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int compareTo(FileItem o) {
        if (this.getName() != null)
            return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }

}