package com.kcorteel.travel_esteban_kylian.travelshare.annotation;

import android.net.Uri;

import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;

public interface TravelShareAnnotationProvider {

    AnnotationSuggestion generateSuggestion(
            Uri imageUri,
            String title,
            String description,
            String placeName,
            String city,
            String country,
            PlaceType placeType
    ) throws Exception;

    boolean isConfigured();
}
