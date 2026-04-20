package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.TypeConverter;

import com.kcorteel.travel_esteban_kylian.travelshare.model.AppTheme;
import com.kcorteel.travel_esteban_kylian.travelshare.model.MediaType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteractionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TravelShareConverters {

    @TypeConverter
    public static String fromTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(";;", tags);
    }

    @TypeConverter
    public static List<String> toTags(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(";;")));
    }

    @TypeConverter
    public static String fromPlaceType(PlaceType placeType) {
        return placeType == null ? null : placeType.name();
    }

    @TypeConverter
    public static PlaceType toPlaceType(String value) {
        return value == null ? PlaceType.OTHER : PlaceType.valueOf(value);
    }

    @TypeConverter
    public static String fromMediaType(MediaType mediaType) {
        return mediaType == null ? null : mediaType.name();
    }

    @TypeConverter
    public static MediaType toMediaType(String value) {
        return value == null ? MediaType.PHOTO : MediaType.valueOf(value);
    }

    @TypeConverter
    public static String fromSocialInteractionType(SocialInteractionType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static SocialInteractionType toSocialInteractionType(String value) {
        return value == null ? SocialInteractionType.LIKE : SocialInteractionType.valueOf(value);
    }

    @TypeConverter
    public static String fromAppTheme(AppTheme theme) {
        return theme == null ? null : theme.name();
    }

    @TypeConverter
    public static AppTheme toAppTheme(String value) {
        return value == null ? AppTheme.SYSTEM : AppTheme.valueOf(value);
    }
}
