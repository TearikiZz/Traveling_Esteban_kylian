package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class PhotoPost {

    private final String title;
    private final String location;
    private final String date;
    private final String description;

    public PhotoPost(String title, String location, String date, String description) {
        this.title = title;
        this.location = location;
        this.date = date;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
