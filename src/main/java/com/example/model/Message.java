package com.example.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Message {
    // Getters
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

    // Setters
    public void setBadgeCount(int badgeCount) { this.badgeCount = badgeCount; }
    public void setText(String text) { this.text = text; }
    public void setDate(String date) { this.date = date; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
