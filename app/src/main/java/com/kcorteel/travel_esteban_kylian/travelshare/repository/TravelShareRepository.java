package com.kcorteel.travel_esteban_kylian.travelshare.repository;

import android.content.Context;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.travelshare.database.CommentDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.LocationDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.MediaDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.PhotoMetadataDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.SocialInteractionDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.TravelShareDatabase;
import com.kcorteel.travel_esteban_kylian.travelshare.database.UserDao;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Comment;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Location;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Media;
import com.kcorteel.travel_esteban_kylian.travelshare.model.MediaType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteraction;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteractionType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TravelShareRepository {

    private static TravelShareRepository instance;

    private final UserDao userDao;
    private final LocationDao locationDao;
    private final MediaDao mediaDao;
    private final PhotoMetadataDao photoMetadataDao;
    private final CommentDao commentDao;
    private final SocialInteractionDao socialInteractionDao;

    private final long currentUserId;

    private TravelShareRepository(Context context) {
        TravelShareDatabase database = TravelShareDatabase.getInstance(context);
        userDao = database.userDao();
        locationDao = database.locationDao();
        mediaDao = database.mediaDao();
        photoMetadataDao = database.photoMetadataDao();
        commentDao = database.commentDao();
        socialInteractionDao = database.socialInteractionDao();

        currentUserId = 1L;

        seedDatabaseIfNeeded();
    }

    public static synchronized TravelShareRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TravelShareRepository(context.getApplicationContext());
        }
        return instance;
    }

    public List<PhotoMetadata> getPhotoMetadataList() {
        return photoMetadataDao.getAll();
    }

    public PhotoMetadata getPhotoMetadataById(long photoId) {
        return photoMetadataDao.getById(photoId);
    }

    public Location getLocationById(long locationId) {
        return locationDao.getById(locationId);
    }

    public Media getMediaById(long mediaId) {
        return mediaDao.getById(mediaId);
    }

    public User getUserById(long userId) {
        return userDao.getById(userId);
    }

    public User getCurrentUser() {
        return userDao.getById(currentUserId);
    }

    public List<Comment> getCommentsForPhoto(long photoId) {
        return commentDao.getByPhotoId(photoId);
    }

    public Comment addComment(long photoId, String text) {
        String normalizedText = text == null ? "" : text.trim();
        if (normalizedText.isEmpty()) {
            return null;
        }

        long nextCommentId = commentDao.getMaxCommentId() + 1L;
        Comment comment = new Comment(
                nextCommentId,
                photoId,
                currentUserId,
                normalizedText,
                "",
                System.currentTimeMillis()
        );
        commentDao.insert(comment);
        return comment;
    }

    public boolean toggleLike(long photoId) {
        SocialInteraction existingLike = socialInteractionDao.findInteraction(
                currentUserId,
                photoId,
                SocialInteractionType.LIKE
        );

        if (existingLike != null) {
            socialInteractionDao.delete(existingLike);
            return false;
        }

        socialInteractionDao.insert(new SocialInteraction(
                socialInteractionDao.getMaxInteractionId() + 1L,
                currentUserId,
                photoId,
                SocialInteractionType.LIKE
        ));
        return true;
    }

    public boolean isPhotoLikedByCurrentUser(long photoId) {
        return socialInteractionDao.findInteraction(currentUserId, photoId, SocialInteractionType.LIKE) != null;
    }

    public int getLikeCount(long photoId) {
        return socialInteractionDao.countByTargetAndType(photoId, SocialInteractionType.LIKE);
    }

    public boolean reportPhoto(long photoId) {
        SocialInteraction existingReport = socialInteractionDao.findInteraction(
                currentUserId,
                photoId,
                SocialInteractionType.REPORT
        );

        if (existingReport != null) {
            return false;
        }

        socialInteractionDao.insert(new SocialInteraction(
                socialInteractionDao.getMaxInteractionId() + 1L,
                currentUserId,
                photoId,
                SocialInteractionType.REPORT
        ));
        return true;
    }

    public boolean isPhotoReportedByCurrentUser(long photoId) {
        return socialInteractionDao.findInteraction(currentUserId, photoId, SocialInteractionType.REPORT) != null;
    }

    public int resolveMediaResourceId(Context context, PhotoMetadata photoMetadata) {
        Media media = getMediaById(photoMetadata.getMediaId());
        if (media == null) {
            return R.drawable.ic_launcher_background;
        }

        int resourceId = context.getResources()
                .getIdentifier(media.getUrl(), "drawable", context.getPackageName());
        return resourceId != 0 ? resourceId : R.drawable.ic_launcher_background;
    }

    public String getLocationLabel(PhotoMetadata photoMetadata) {
        Location location = getLocationById(photoMetadata.getLocationId());
        if (location == null) {
            return "";
        }
        return location.getCity() + ", " + location.getCountry();
    }

    public String getAuthorLabel(PhotoMetadata photoMetadata) {
        User user = getUserById(photoMetadata.getAuthorId());
        return user == null ? "" : user.getUsername();
    }

    public String getRouteAdvice(PhotoMetadata photoMetadata) {
        Location location = getLocationById(photoMetadata.getLocationId());
        if (location == null) {
            return "";
        }

        return "Rejoindre " + location.getAddress()
                + ", " + location.getCity()
                + ". Ouvrez l'itinéraire pour un guidage détaillé jusqu'au point photo.";
    }

    public String getSearchableText(PhotoMetadata photoMetadata) {
        Location location = getLocationById(photoMetadata.getLocationId());
        StringBuilder builder = new StringBuilder();
        builder.append(photoMetadata.getTitle()).append(' ')
                .append(photoMetadata.getDescription()).append(' ')
                .append(photoMetadata.getPlaceType().name()).append(' ');

        if (location != null) {
            builder.append(location.getAddress()).append(' ')
                    .append(location.getCity()).append(' ')
                    .append(location.getCountry()).append(' ');
        }

        for (String tag : photoMetadata.getTags()) {
            builder.append(tag).append(' ');
        }

        return builder.toString().toLowerCase(Locale.getDefault());
    }

    private void seedDatabaseIfNeeded() {
        if (photoMetadataDao.countAll() > 0) {
            return;
        }

        userDao.insertAll(Arrays.asList(
                new User(1L, "kylian", "kylian@traveling.app", "hash-kylian", false),
                new User(2L, "esteban", "esteban@traveling.app", "hash-esteban", false),
                new User(3L, "maya", "maya@traveling.app", "hash-maya", false),
                new User(4L, "voyage_anonyme", "", "", true)
        ));

        locationDao.insertAll(Arrays.asList(
                new Location(101L, 48.8584, 2.2945, "Champ de Mars", "Paris", "France"),
                new Location(102L, 35.0116, 135.7681, "Quartier de Gion", "Kyoto", "Japon"),
                new Location(103L, 41.8902, 12.4922, "Piazza del Colosseo", "Rome", "Italie"),
                new Location(104L, 41.3851, 2.1734, "Barri Gotic", "Barcelone", "Espagne")
        ));

        mediaDao.insertAll(Arrays.asList(
                new Media(201L, 2L, "img_mock_paris", MediaType.PHOTO, "img_mock_paris"),
                new Media(202L, 3L, "img_mock_kyoto", MediaType.PHOTO, "img_mock_kyoto"),
                new Media(203L, 2L, "img_mock_rome", MediaType.PHOTO, "img_mock_rome"),
                new Media(204L, 1L, "img_mock_barcelona", MediaType.PHOTO, "img_mock_barcelona")
        ));

        photoMetadataDao.insertAll(Arrays.asList(
                new PhotoMetadata(
                        301L,
                        2L,
                        "Balade au lever du soleil",
                        "Une promenade matinale le long de la Seine avec une vue magnifique sur la Tour Eiffel.",
                        1775944800000L,
                        101L,
                        201L,
                        Arrays.asList("sunrise", "eiffel", "seine"),
                        PlaceType.STREET
                ),
                new PhotoMetadata(
                        302L,
                        3L,
                        "Temples et cerisiers",
                        "Une journée entre sanctuaires, ruelles traditionnelles et fleurs de cerisier en pleine saison.",
                        1772586000000L,
                        102L,
                        202L,
                        Arrays.asList("sakura", "temple", "gion"),
                        PlaceType.MUSEUM
                ),
                new PhotoMetadata(
                        303L,
                        2L,
                        "Escapade historique",
                        "Découverte du Colisée, des places animées et d'une cuisine italienne pleine de saveurs.",
                        1771138800000L,
                        103L,
                        203L,
                        Arrays.asList("rome", "colosseum", "history"),
                        PlaceType.MUSEUM
                ),
                new PhotoMetadata(
                        304L,
                        1L,
                        "Ambiance méditerranéenne",
                        "Entre architecture colorée, bord de mer et tapas partagées au coucher du soleil.",
                        1769500800000L,
                        104L,
                        204L,
                        Arrays.asList("mediterranean", "sea", "tapas"),
                        PlaceType.RESTAURANT
                )
        ));

        commentDao.insertAll(Arrays.asList(
                new Comment(1000L, 301L, 1L, "La lumière est incroyable, on sent vraiment l'ambiance du matin.", "", 1775948400000L),
                new Comment(1001L, 301L, 3L, "Super point de vue, je vais l'ajouter à mon prochain city trip.", "", 1775950200000L),
                new Comment(1002L, 302L, 2L, "Le contraste entre les ruelles et les arbres en fleurs marche vraiment bien.", "", 1772589900000L),
                new Comment(1003L, 304L, 2L, "On imagine déjà la fin de journée au bord de l'eau.", "", 1769505300000L)
        ));

        socialInteractionDao.insertAll(Arrays.asList(
                new SocialInteraction(1000L, 1L, 301L, SocialInteractionType.LIKE),
                new SocialInteraction(1001L, 2L, 301L, SocialInteractionType.LIKE),
                new SocialInteraction(1002L, 3L, 301L, SocialInteractionType.LIKE),
                new SocialInteraction(1003L, 1L, 304L, SocialInteractionType.LIKE),
                new SocialInteraction(1004L, 3L, 304L, SocialInteractionType.LIKE)
        ));
    }
}
