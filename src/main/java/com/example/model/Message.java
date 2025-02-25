package com.example.model;

public class Message {
    private int id;
    private String name;
    private String about;
    private int badgeCount;
    private String text;
    private String date;
    private String img;

    public Message(int id, String name, String about, int badgeCount, String text, String date, String img) {
        this.id = id;
        this.name = name;
        this.about = about;
        this.badgeCount = badgeCount;
        this.text = text;
        this.date = date;
        this.img = img;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAbout() { return about; }
    public int getBadgeCount() { return badgeCount; }
    public String getText() { return text; }
    public String getDate() { return date; }
    public String getImg() { return img; }

    // Setters
    public void setBadgeCount(int badgeCount) { this.badgeCount = badgeCount; }
    public void setText(String text) { this.text = text; }
    public void setDate(String date) { this.date = date; }
}
