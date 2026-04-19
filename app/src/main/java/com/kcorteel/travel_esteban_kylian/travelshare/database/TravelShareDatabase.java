package com.kcorteel.travel_esteban_kylian.travelshare.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kcorteel.travel_esteban_kylian.travelshare.model.Comment;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Location;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Media;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteraction;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;

@Database(
        entities = {
                User.class,
                Location.class,
                Media.class,
                PhotoMetadata.class,
                Comment.class,
                SocialInteraction.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters(TravelShareConverters.class)
public abstract class TravelShareDatabase extends RoomDatabase {

    private static volatile TravelShareDatabase instance;

    public abstract UserDao userDao();

    public abstract LocationDao locationDao();

    public abstract MediaDao mediaDao();

    public abstract PhotoMetadataDao photoMetadataDao();

    public abstract CommentDao commentDao();

    public abstract SocialInteractionDao socialInteractionDao();

    public static TravelShareDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (TravelShareDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TravelShareDatabase.class,
                                    "travelshare.db"
                            )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
