package com.mandakini.memonest.models;

public class Draft {

    private int id;
    private String title;
    private String content;
    private String imageUri;
    private String createdAt;
    private int isUploaded;

    public Draft() {
    }

    public Draft(int id, String title, String content, String imageUri, String createdAt, int isUploaded) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
        this.isUploaded = isUploaded;
    }

    public Draft(String title, String content, String imageUri, String createdAt, int isUploaded) {
        this.title = title;
        this.content = content;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
        this.isUploaded = isUploaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public int getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(int isUploaded) {
        this.isUploaded = isUploaded;
    }
}