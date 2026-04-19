package com.kcorteel.travel_esteban_kylian.travelshare.model;

import java.io.Serializable;

public class PhotoPost implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String title;
    private final String location;
    private final String date;
    private final String description;
    private final String routeAdvice;
    private final int imageResId;
    private final double latitude;
    private final double longitude;
    private boolean liked;

    public PhotoPost(
            String title,
            String location,
            String date,
            String description,
            String routeAdvice,
            int imageResId,
            double latitude,
            double longitude,
            boolean liked
    ) {
        this.title = title;
        this.location = location;
        this.date = date;
        this.description = description;
        this.routeAdvice = routeAdvice;
        this.imageResId = imageResId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.liked = liked;
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

    public String getRouteAdvice() {
        return routeAdvice;
    }

    public int getImageResId() {
        return imageResId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
