package com.lux.ex091firebasechatting;

public class MessageItem {
    public String name;
    public String message;
    public String time;
    public String profileUrl;

    public MessageItem() {
    }

    public MessageItem(String name, String message, String time, String profileUrl) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.profileUrl = profileUrl;
    }
}
