package com.kcorteel.travel_esteban_kylian.travelshare.repository;

import android.content.Context;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Comment;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Location;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Media;
import com.kcorteel.travel_esteban_kylian.travelshare.model.MediaType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteraction;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteractionType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TravelShareRepository {

    private static TravelShareRepository instance;

    private final Map<Long, User> usersById;
    private final Map<Long, Location> locationsById;
    private final Map<Long, Media> mediaById;
    private final List<PhotoMetadata> photoMetadataList;
    private final List<Comment> commentList;
    private final List<SocialInteraction> socialInteractionList;

    private long nextCommentId;
    private long nextInteractionId;

    private final long currentUserId;

    private TravelShareRepository() {
        usersById = new HashMap<>();
        locationsById = new HashMap<>();
        mediaById = new HashMap<>();
        photoMetadataList = new ArrayList<>();
        commentList = new ArrayList<>();
        socialInteractionList = new ArrayList<>();

        currentUserId = 1L;
        nextCommentId = 1000L;
        nextInteractionId = 1000L;

        seedUsers();
        seedLocations();
        seedMedia();
        seedPhotoMetadata();
        seedComments();
        seedSocialInteractions();
    }

    public static synchronized TravelShareRepository getInstance() {
        if (instance == null) {
            instance = new TravelShareRepository();
        }
        return instance;
    }

    public List<PhotoMetadata> getPhotoMetadataList() {
        return new ArrayList<>(photoMetadataList);
    }

    public PhotoMetadata getPhotoMetadataById(long photoId) {
        for (PhotoMetadata photoMetadata : photoMetadataList) {
            if (photoMetadata.getPhotoId() == photoId) {
                return photoMetadata;
            }
        }
        return null;
    }

    public Location getLocationById(long locationId) {
        return locationsById.get(locationId);
    }

    public Media getMediaById(long mediaId) {
        return mediaById.get(mediaId);
    }

    public User getUserById(long userId) {
        return usersById.get(userId);
    }

    public User getCurrentUser() {
        return usersById.get(currentUserId);
    }

    public List<Comment> getCommentsForPhoto(long photoId) {
        List<Comment> commentsForPhoto = new ArrayList<>();
        for (Comment comment : commentList) {
            if (comment.getPhotoId() == photoId) {
                commentsForPhoto.add(comment);
            }
        }
        commentsForPhoto.sort(Comparator.comparingLong(Comment::getCreatedAt));
        return commentsForPhoto;
    }

    public Comment addComment(long photoId, String text) {
        String normalizedText = text == null ? "" : text.trim();
        if (normalizedText.isEmpty()) {
            return null;
        }

        Comment comment = new Comment(
                nextCommentId++,
                photoId,
                currentUserId,
                normalizedText,
                "",
                System.currentTimeMillis()
        );
        commentList.add(comment);
        return comment;
    }

    public boolean toggleLike(long photoId) {
        SocialInteraction existingLike = findInteraction(currentUserId, photoId, SocialInteractionType.LIKE);
        if (existingLike != null) {
            socialInteractionList.remove(existingLike);
            return false;
        }

        socialInteractionList.add(new SocialInteraction(
                nextInteractionId++,
                currentUserId,
                photoId,
                SocialInteractionType.LIKE
        ));
        return true;
    }

    public boolean isPhotoLikedByCurrentUser(long photoId) {
        return findInteraction(currentUserId, photoId, SocialInteractionType.LIKE) != null;
    }

    public int getLikeCount(long photoId) {
        int count = 0;
        for (SocialInteraction socialInteraction : socialInteractionList) {
            if (socialInteraction.getTargetId() == photoId
                    && socialInteraction.getType() == SocialInteractionType.LIKE) {
                count++;
            }
        }
        return count;
    }

    public boolean reportPhoto(long photoId) {
        SocialInteraction existingReport = findInteraction(currentUserId, photoId, SocialInteractionType.REPORT);
        if (existingReport != null) {
            return false;
        }

        socialInteractionList.add(new SocialInteraction(
                nextInteractionId++,
                currentUserId,
                photoId,
                SocialInteractionType.REPORT
        ));
        return true;
    }

    public boolean isPhotoReportedByCurrentUser(long photoId) {
        return findInteraction(currentUserId, photoId, SocialInteractionType.REPORT) != null;
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

    private SocialInteraction findInteraction(long userId, long photoId, SocialInteractionType type) {
        for (SocialInteraction socialInteraction : socialInteractionList) {
            if (socialInteraction.getUserId() == userId
                    && socialInteraction.getTargetId() == photoId
                    && socialInteraction.getType() == type) {
                return socialInteraction;
            }
        }
        return null;
    }

    private void seedUsers() {
        usersById.put(1L, new User(1L, "kylian", "kylian@traveling.app", "hash-kylian", false));
        usersById.put(2L, new User(2L, "esteban", "esteban@traveling.app", "hash-esteban", false));
        usersById.put(3L, new User(3L, "maya", "maya@traveling.app", "hash-maya", false));
        usersById.put(4L, new User(4L, "voyage_anonyme", "", "", true));
    }

    private void seedLocations() {
        locationsById.put(101L, new Location(101L, 48.8584, 2.2945, "Champ de Mars", "Paris", "France"));
        locationsById.put(102L, new Location(102L, 35.0116, 135.7681, "Quartier de Gion", "Kyoto", "Japon"));
        locationsById.put(103L, new Location(103L, 41.8902, 12.4922, "Piazza del Colosseo", "Rome", "Italie"));
        locationsById.put(104L, new Location(104L, 41.3851, 2.1734, "Barri Gotic", "Barcelone", "Espagne"));
    }

    private void seedMedia() {
        mediaById.put(201L, new Media(201L, 2L, "img_mock_paris", MediaType.PHOTO, "img_mock_paris"));
        mediaById.put(202L, new Media(202L, 3L, "img_mock_kyoto", MediaType.PHOTO, "img_mock_kyoto"));
        mediaById.put(203L, new Media(203L, 2L, "img_mock_rome", MediaType.PHOTO, "img_mock_rome"));
        mediaById.put(204L, new Media(204L, 1L, "img_mock_barcelona", MediaType.PHOTO, "img_mock_barcelona"));
    }

    private void seedPhotoMetadata() {
        photoMetadataList.add(new PhotoMetadata(
                301L,
                2L,
                "Balade au lever du soleil",
                "Une promenade matinale le long de la Seine avec une vue magnifique sur la Tour Eiffel.",
                1775944800000L,
                101L,
                201L,
                Arrays.asList("sunrise", "eiffel", "seine"),
                PlaceType.STREET
        ));

        photoMetadataList.add(new PhotoMetadata(
                302L,
                3L,
                "Temples et cerisiers",
                "Une journée entre sanctuaires, ruelles traditionnelles et fleurs de cerisier en pleine saison.",
                1772586000000L,
                102L,
                202L,
                Arrays.asList("sakura", "temple", "gion"),
                PlaceType.MUSEUM
        ));

        photoMetadataList.add(new PhotoMetadata(
                303L,
                2L,
                "Escapade historique",
                "Découverte du Colisée, des places animées et d'une cuisine italienne pleine de saveurs.",
                1771138800000L,
                103L,
                203L,
                Arrays.asList("rome", "colosseum", "history"),
                PlaceType.MUSEUM
        ));

        photoMetadataList.add(new PhotoMetadata(
                304L,
                1L,
                "Ambiance méditerranéenne",
                "Entre architecture colorée, bord de mer et tapas partagées au coucher du soleil.",
                1769500800000L,
                104L,
                204L,
                Arrays.asList("mediterranean", "sea", "tapas"),
                PlaceType.RESTAURANT
        ));
    }

    private void seedComments() {
        commentList.add(new Comment(
                nextCommentId++,
                301L,
                1L,
                "La lumière est incroyable, on sent vraiment l'ambiance du matin.",
                "",
                1775948400000L
        ));
        commentList.add(new Comment(
                nextCommentId++,
                301L,
                3L,
                "Super point de vue, je vais l'ajouter à mon prochain city trip.",
                "",
                1775950200000L
        ));
        commentList.add(new Comment(
                nextCommentId++,
                302L,
                2L,
                "Le contraste entre les ruelles et les arbres en fleurs marche vraiment bien.",
                "",
                1772589900000L
        ));
        commentList.add(new Comment(
                nextCommentId++,
                304L,
                2L,
                "On imagine déjà la fin de journée au bord de l'eau.",
                "",
                1769505300000L
        ));
    }

    private void seedSocialInteractions() {
        socialInteractionList.add(new SocialInteraction(nextInteractionId++, 1L, 301L, SocialInteractionType.LIKE));
        socialInteractionList.add(new SocialInteraction(nextInteractionId++, 2L, 301L, SocialInteractionType.LIKE));
        socialInteractionList.add(new SocialInteraction(nextInteractionId++, 3L, 301L, SocialInteractionType.LIKE));
        socialInteractionList.add(new SocialInteraction(nextInteractionId++, 1L, 304L, SocialInteractionType.LIKE));
        socialInteractionList.add(new SocialInteraction(nextInteractionId++, 3L, 304L, SocialInteractionType.LIKE));
    }
}
