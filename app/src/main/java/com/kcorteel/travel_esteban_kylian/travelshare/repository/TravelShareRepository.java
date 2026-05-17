package com.kcorteel.travel_esteban_kylian.travelshare.repository;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatDelegate;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.auth.AppSessionManager;
import com.kcorteel.travel_esteban_kylian.auth.PasswordUtils;
import com.kcorteel.travel_esteban_kylian.travelshare.database.AppPreferencesDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.CommentDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.GroupMembershipDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.LocationDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.MediaDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.NotificationDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.PhotoMetadataDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.SocialInteractionDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.TravelGroupDao;
import com.kcorteel.travel_esteban_kylian.travelshare.database.TravelShareDatabase;
import com.kcorteel.travel_esteban_kylian.travelshare.database.UserDao;
import com.kcorteel.travel_esteban_kylian.travelshare.model.AppPreferences;
import com.kcorteel.travel_esteban_kylian.travelshare.model.AppTheme;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Comment;
import com.kcorteel.travel_esteban_kylian.travelshare.model.GroupMembership;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Location;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Media;
import com.kcorteel.travel_esteban_kylian.travelshare.model.MediaType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Notification;
import com.kcorteel.travel_esteban_kylian.travelshare.model.NotificationTriggerType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteraction;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteractionType;
import com.kcorteel.travel_esteban_kylian.travelshare.model.TravelGroup;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;
import com.kcorteel.travel_esteban_kylian.travelshare.notifications.TravelShareSystemNotifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private final AppPreferencesDao appPreferencesDao;
    private final NotificationDao notificationDao;
    private final TravelGroupDao travelGroupDao;
    private final GroupMembershipDao groupMembershipDao;

    private final AppSessionManager appSessionManager;
    private final Context appContext;

    private TravelShareRepository(Context context) {
        appContext = context.getApplicationContext();
        TravelShareDatabase database = TravelShareDatabase.getInstance(context);
        userDao = database.userDao();
        locationDao = database.locationDao();
        mediaDao = database.mediaDao();
        photoMetadataDao = database.photoMetadataDao();
        commentDao = database.commentDao();
        socialInteractionDao = database.socialInteractionDao();
        appPreferencesDao = database.appPreferencesDao();
        notificationDao = database.notificationDao();
        travelGroupDao = database.travelGroupDao();
        groupMembershipDao = database.groupMembershipDao();
        appSessionManager = new AppSessionManager(context);

        seedDatabaseIfNeeded();
    }

    public static synchronized TravelShareRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TravelShareRepository(context.getApplicationContext());
        }
        return instance;
    }

    public List<PhotoMetadata> getPhotoMetadataList() {
        List<PhotoMetadata> result = new ArrayList<>();
        for (PhotoMetadata photoMetadata : photoMetadataDao.getAll()) {
            if (canAccessPhotoMetadata(photoMetadata)) {
                result.add(photoMetadata);
            }
        }
        return result;
    }

    public PhotoMetadata getPhotoMetadataById(long photoId) {
        PhotoMetadata photoMetadata = photoMetadataDao.getById(photoId);
        return canAccessPhotoMetadata(photoMetadata) ? photoMetadata : null;
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
        return userDao.getById(appSessionManager.getCurrentUserId());
    }

    public boolean isCurrentUserAnonymous() {
        User currentUser = getCurrentUser();
        return currentUser == null || currentUser.isAnonymous();
    }

    public AppPreferences getCurrentUserPreferences() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.isAnonymous()) {
            return new AppPreferences(0L, appSessionManager.getCurrentUserId(), AppTheme.SYSTEM, "fr", false);
        }
        return getOrCreatePreferencesForUser(currentUser.getUserId());
    }

    public String updateCurrentUserProfile(String username, String email, String avatarUri) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.isAnonymous()) {
            return "Connectez-vous pour modifier votre profil.";
        }

        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedAvatarUri = avatarUri == null
                ? currentUser.getAvatarUri()
                : avatarUri.trim();

        if (normalizedUsername.isEmpty() || normalizedEmail.isEmpty()) {
            return "Le nom d'utilisateur et l'email sont obligatoires.";
        }

        User existingByUsername = userDao.getByUsername(normalizedUsername);
        if (existingByUsername != null && existingByUsername.getUserId() != currentUser.getUserId()) {
            return "Ce nom d'utilisateur existe déjà.";
        }

        User existingByEmail = userDao.getByEmail(normalizedEmail);
        if (existingByEmail != null && existingByEmail.getUserId() != currentUser.getUserId()) {
            return "Cet email est déjà utilisé.";
        }

        userDao.upsert(new User(
                currentUser.getUserId(),
                normalizedUsername,
                normalizedEmail,
                currentUser.getPasswordHash(),
                false,
                normalizedAvatarUri
        ));

        return null;
    }

    public void updateCurrentUserPreferences(AppTheme theme, String language, boolean notificationsEnabled) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.isAnonymous()) {
            return;
        }

        appPreferencesDao.insert(new AppPreferences(
                currentUser.getUserId(),
                currentUser.getUserId(),
                theme == null ? AppTheme.SYSTEM : theme,
                TextUtils.isEmpty(language) ? "fr" : language,
                notificationsEnabled
        ));
    }

    public void applyCurrentUserThemePreference() {
        AppTheme theme = getCurrentUserPreferences().getTheme();
        int nightMode;
        switch (theme) {
            case LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case SYSTEM:
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public ProfileStats getCurrentUserProfileStats() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.isAnonymous()) {
            return new ProfileStats(0, 0, 0);
        }

        long userId = currentUser.getUserId();
        return new ProfileStats(
                photoMetadataDao.countByAuthorId(userId),
                commentDao.countByUserId(userId),
                socialInteractionDao.countLikesReceivedByAuthor(userId)
        );
    }

    public PhotoMetadata createPhotoMetadata(
            String title,
            String description,
            String address,
            String city,
            String country,
            double latitude,
            double longitude,
            List<String> tags,
            PlaceType placeType,
            Long groupId,
            String audioNoteUrl,
            String imageDrawableName
    ) {
        if (isCurrentUserAnonymous()) {
            return null;
        }

        if (groupId != null && !isCurrentUserMemberOfGroup(groupId)) {
            return null;
        }

        long nextLocationId = locationDao.getMaxLocationId() + 1L;
        long nextMediaId = mediaDao.getMaxMediaId() + 1L;
        long nextPhotoId = photoMetadataDao.getMaxPhotoId() + 1L;

        Location location = new Location(nextLocationId, latitude, longitude, address, city, country);
        Media media = new Media(nextMediaId, appSessionManager.getCurrentUserId(), imageDrawableName, MediaType.PHOTO, imageDrawableName);
        PhotoMetadata photoMetadata = new PhotoMetadata(
                nextPhotoId,
                appSessionManager.getCurrentUserId(),
                title,
                description,
                System.currentTimeMillis(),
                nextLocationId,
                nextMediaId,
                groupId,
                audioNoteUrl,
                tags == null ? Collections.emptyList() : tags,
                placeType
        );

        locationDao.insert(location);
        mediaDao.insert(media);
        photoMetadataDao.insert(photoMetadata);
        if (groupId == null) {
            notifyUsersAboutNewPost(photoMetadata);
        } else {
            notifyGroupMembersAboutNewPost(photoMetadata);
        }

        return photoMetadata;
    }

    public List<TravelGroup> getVisibleGroups() {
        List<TravelGroup> result = new ArrayList<>();
        for (TravelGroup group : travelGroupDao.getAll()) {
            if (canAccessGroup(group)) {
                result.add(group);
            }
        }
        return result;
    }

    public TravelGroup getGroupById(long groupId) {
        TravelGroup group = travelGroupDao.getById(groupId);
        return canAccessGroup(group) ? group : null;
    }

    public List<TravelGroup> getGroupsForCurrentUser() {
        if (isCurrentUserAnonymous()) {
            return Collections.emptyList();
        }

        List<TravelGroup> groups = new ArrayList<>();
        for (GroupMembership membership : groupMembershipDao.getByUserId(appSessionManager.getCurrentUserId())) {
            TravelGroup group = travelGroupDao.getById(membership.getGroupId());
            if (group != null) {
                groups.add(group);
            }
        }
        groups.sort((left, right) -> left.getGroupName().compareToIgnoreCase(right.getGroupName()));
        return groups;
    }

    public String createGroup(String groupName, boolean isPrivate) {
        if (isCurrentUserAnonymous()) {
            return "Connectez-vous pour créer un groupe.";
        }

        String normalizedName = groupName == null ? "" : groupName.trim();
        if (normalizedName.isEmpty()) {
            return "Le nom du groupe est obligatoire.";
        }

        for (TravelGroup existingGroup : travelGroupDao.getAll()) {
            if (existingGroup.getGroupName().equalsIgnoreCase(normalizedName)) {
                return "Un groupe avec ce nom existe déjà.";
            }
        }

        long groupId = travelGroupDao.getMaxGroupId() + 1L;
        long currentUserId = appSessionManager.getCurrentUserId();
        travelGroupDao.insert(new TravelGroup(groupId, normalizedName, currentUserId, isPrivate));
        groupMembershipDao.insert(new GroupMembership(groupId, currentUserId));
        return null;
    }

    public boolean joinGroup(long groupId) {
        if (isCurrentUserAnonymous()) {
            return false;
        }

        TravelGroup group = travelGroupDao.getById(groupId);
        if (!canAccessGroup(group)) {
            return false;
        }
        if (isCurrentUserMemberOfGroup(groupId)) {
            return true;
        }

        groupMembershipDao.insert(new GroupMembership(groupId, appSessionManager.getCurrentUserId()));
        return true;
    }

    public boolean leaveGroup(long groupId) {
        if (isCurrentUserAnonymous()) {
            return false;
        }

        TravelGroup group = travelGroupDao.getById(groupId);
        if (group == null || group.getCreatorId() == appSessionManager.getCurrentUserId()) {
            return false;
        }

        GroupMembership membership = groupMembershipDao.getByGroupIdAndUserId(
                groupId,
                appSessionManager.getCurrentUserId()
        );
        if (membership == null) {
            return false;
        }

        groupMembershipDao.delete(membership);
        return true;
    }

    public boolean isCurrentUserMemberOfGroup(long groupId) {
        if (isCurrentUserAnonymous()) {
            return false;
        }
        return groupMembershipDao.getByGroupIdAndUserId(groupId, appSessionManager.getCurrentUserId()) != null;
    }

    public int getGroupMemberCount(long groupId) {
        return groupMembershipDao.countByGroupId(groupId);
    }

    public String getGroupCreatorLabel(TravelGroup group) {
        if (group == null) {
            return "";
        }
        User creator = getUserById(group.getCreatorId());
        return creator == null ? getStringResource(R.string.travelshare_unknown_user) : creator.getUsername();
    }

    public List<PhotoMetadata> getPhotoMetadataForGroup(long groupId) {
        TravelGroup group = getGroupById(groupId);
        if (group == null) {
            return Collections.emptyList();
        }

        List<PhotoMetadata> result = new ArrayList<>();
        for (PhotoMetadata photoMetadata : getPhotoMetadataList()) {
            if (photoMetadata.getGroupId() != null && photoMetadata.getGroupId() == groupId) {
                result.add(photoMetadata);
            }
        }
        return result;
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
                appSessionManager.getCurrentUserId(),
                normalizedText,
                "",
                System.currentTimeMillis()
        );
        commentDao.insert(comment);
        notifyPhotoAuthorAboutComment(photoId, normalizedText);
        return comment;
    }

    public boolean toggleLike(long photoId) {
        SocialInteraction existingLike = socialInteractionDao.findInteraction(
                appSessionManager.getCurrentUserId(),
                photoId,
                SocialInteractionType.LIKE
        );

        if (existingLike != null) {
            socialInteractionDao.delete(existingLike);
            return false;
        }

        socialInteractionDao.insert(new SocialInteraction(
                socialInteractionDao.getMaxInteractionId() + 1L,
                appSessionManager.getCurrentUserId(),
                photoId,
                SocialInteractionType.LIKE
        ));
        notifyPhotoAuthorAboutLike(photoId);
        return true;
    }

    public boolean isPhotoLikedByCurrentUser(long photoId) {
        return socialInteractionDao.findInteraction(
                appSessionManager.getCurrentUserId(),
                photoId,
                SocialInteractionType.LIKE
        ) != null;
    }

    public int getLikeCount(long photoId) {
        return socialInteractionDao.countByTargetAndType(photoId, SocialInteractionType.LIKE);
    }

    public boolean reportPhoto(long photoId) {
        SocialInteraction existingReport = socialInteractionDao.findInteraction(
                appSessionManager.getCurrentUserId(),
                photoId,
                SocialInteractionType.REPORT
        );

        if (existingReport != null) {
            return false;
        }

        socialInteractionDao.insert(new SocialInteraction(
                socialInteractionDao.getMaxInteractionId() + 1L,
                appSessionManager.getCurrentUserId(),
                photoId,
                SocialInteractionType.REPORT
        ));
        return true;
    }

    public boolean isPhotoReportedByCurrentUser(long photoId) {
        return socialInteractionDao.findInteraction(
                appSessionManager.getCurrentUserId(),
                photoId,
                SocialInteractionType.REPORT
        ) != null;
    }

    public List<Notification> getNotificationsForCurrentUser() {
        return notificationDao.getByTargetUserId(appSessionManager.getCurrentUserId());
    }

    public int getUnreadNotificationsCountForCurrentUser() {
        return notificationDao.countUnreadByTargetUserId(appSessionManager.getCurrentUserId());
    }

    public void markCurrentUserNotificationsAsRead() {
        notificationDao.markAllAsRead(appSessionManager.getCurrentUserId());
    }

    public void dispatchPendingSystemNotifications() {
        if (isCurrentUserAnonymous()) {
            return;
        }

        for (Notification notification : notificationDao.getUndeliveredByTargetUserId(appSessionManager.getCurrentUserId())) {
            if (!getCurrentUserPreferences().isNotificationsEnabled()) {
                return;
            }

            boolean shown = TravelShareSystemNotifier.showNotification(
                    appContext,
                    notification,
                    getNotificationTypeLabel(notification)
            );
            if (shown) {
                notificationDao.markAsDelivered(notification.getNotifId());
            }
        }
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

    public void loadMediaIntoImageView(Context context, ImageView imageView, PhotoMetadata photoMetadata) {
        Media media = getMediaById(photoMetadata.getMediaId());
        if (media == null || media.getUrl() == null || media.getUrl().trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        String mediaUrl = media.getUrl();
        if (mediaUrl.startsWith("content://") || mediaUrl.startsWith("file://")) {
            imageView.setImageURI(Uri.parse(mediaUrl));
            return;
        }

        int resourceId = context.getResources()
                .getIdentifier(mediaUrl, "drawable", context.getPackageName());
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    public void loadUserAvatarIntoImageView(ImageView imageView, User user) {
        if (user == null || user.getAvatarUri() == null || user.getAvatarUri().trim().isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_myplaces);
            imageView.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF0F172A));
            return;
        }

        String avatarUri = user.getAvatarUri().trim();
        if (avatarUri.startsWith("content://") || avatarUri.startsWith("file://")) {
            imageView.setImageURI(Uri.parse(avatarUri));
            imageView.setImageTintList(null);
            return;
        }

        imageView.setImageResource(android.R.drawable.ic_menu_myplaces);
        imageView.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF0F172A));
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

        if (Double.isNaN(location.getLatitude()) || Double.isNaN(location.getLongitude())) {
            return "Lieu saisi manuellement sans coordonnees precises. Ajoutez une latitude et une longitude pour activer l'itineraire.";
        }

        return "Rejoindre " + location.getAddress()
                + ", " + location.getCity()
                + ". Ouvrez l'itinéraire pour un guidage détaillé jusqu'au point photo.";
    }

    public String getSearchableText(PhotoMetadata photoMetadata) {
        Location location = getLocationById(photoMetadata.getLocationId());
        User author = getUserById(photoMetadata.getAuthorId());
        TravelGroup group = photoMetadata.getGroupId() == null ? null : travelGroupDao.getById(photoMetadata.getGroupId());
        StringBuilder builder = new StringBuilder();
        builder.append(photoMetadata.getTitle()).append(' ')
                .append(photoMetadata.getDescription()).append(' ')
                .append(photoMetadata.getPlaceType().name()).append(' ');

        if (author != null) {
            builder.append(author.getUsername()).append(' ');
        }

        if (location != null) {
            builder.append(location.getAddress()).append(' ')
                    .append(location.getCity()).append(' ')
                    .append(location.getCountry()).append(' ');
        }

        if (group != null) {
            builder.append(group.getGroupName()).append(' ');
        }

        for (String tag : photoMetadata.getTags()) {
            builder.append(tag).append(' ');
        }

        return builder.toString().toLowerCase(Locale.getDefault());
    }

    public String getNotificationTypeLabel(Notification notification) {
        if (notification == null) {
            return "";
        }

        switch (notification.getTriggerType()) {
            case NEW_POST_IN_GROUP:
                return "Publication de groupe";
            case LIKE_ON_PHOTO:
                return "Like reçu";
            case COMMENT_ON_PHOTO:
                return "Commentaire reçu";
            case NEW_POST_BY_USER:
            case NEW_POST_IN_LOCATION:
            case NEW_TAG_MATCH:
            default:
                return "Nouvelle publication";
        }
    }

    public String getGroupLabel(PhotoMetadata photoMetadata) {
        if (photoMetadata == null || photoMetadata.getGroupId() == null) {
            return "";
        }

        TravelGroup group = travelGroupDao.getById(photoMetadata.getGroupId());
        return group == null ? "" : group.getGroupName();
    }

    private void notifyUsersAboutNewPost(PhotoMetadata photoMetadata) {
        User author = getUserById(photoMetadata.getAuthorId());
        if (author == null) {
            return;
        }

        for (User user : userDao.getAllRegisteredUsers()) {
            if (user.getUserId() == photoMetadata.getAuthorId()) {
                continue;
            }
            if (!getOrCreatePreferencesForUser(user.getUserId()).isNotificationsEnabled()) {
                continue;
            }

            createNotification(
                    user.getUserId(),
                    photoMetadata.getPhotoId(),
                    author.getUsername() + " a publié \"" + photoMetadata.getTitle() + "\".",
                    NotificationTriggerType.NEW_POST_BY_USER
            );
        }
    }

    private void notifyGroupMembersAboutNewPost(PhotoMetadata photoMetadata) {
        if (photoMetadata == null || photoMetadata.getGroupId() == null) {
            return;
        }

        TravelGroup group = travelGroupDao.getById(photoMetadata.getGroupId());
        User author = getUserById(photoMetadata.getAuthorId());
        if (group == null || author == null) {
            return;
        }

        for (GroupMembership membership : groupMembershipDao.getByGroupId(group.getGroupId())) {
            if (membership.getUserId() == photoMetadata.getAuthorId()) {
                continue;
            }
            if (!getOrCreatePreferencesForUser(membership.getUserId()).isNotificationsEnabled()) {
                continue;
            }

            createNotification(
                    membership.getUserId(),
                    photoMetadata.getPhotoId(),
                    author.getUsername() + " a publié dans le groupe \"" + group.getGroupName() + "\".",
                    NotificationTriggerType.NEW_POST_IN_GROUP
            );
        }
    }

    private void notifyPhotoAuthorAboutComment(long photoId, String commentText) {
        PhotoMetadata photoMetadata = getPhotoMetadataById(photoId);
        User actor = getCurrentUser();
        if (photoMetadata == null || actor == null || actor.isAnonymous()) {
            return;
        }
        if (photoMetadata.getAuthorId() == actor.getUserId()) {
            return;
        }
        if (!getOrCreatePreferencesForUser(photoMetadata.getAuthorId()).isNotificationsEnabled()) {
            return;
        }

        createNotification(
                photoMetadata.getAuthorId(),
                photoId,
                actor.getUsername() + " a commenté votre publication : \"" + commentText + "\".",
                NotificationTriggerType.COMMENT_ON_PHOTO
        );
    }

    private void notifyPhotoAuthorAboutLike(long photoId) {
        PhotoMetadata photoMetadata = getPhotoMetadataById(photoId);
        User actor = getCurrentUser();
        if (photoMetadata == null || actor == null || actor.isAnonymous()) {
            return;
        }
        if (photoMetadata.getAuthorId() == actor.getUserId()) {
            return;
        }
        if (!getOrCreatePreferencesForUser(photoMetadata.getAuthorId()).isNotificationsEnabled()) {
            return;
        }

        createNotification(
                photoMetadata.getAuthorId(),
                photoId,
                actor.getUsername() + " aime votre publication \"" + photoMetadata.getTitle() + "\".",
                NotificationTriggerType.LIKE_ON_PHOTO
        );
    }

    private void createNotification(
            long targetUserId,
            long relatedPhotoId,
            String message,
            NotificationTriggerType triggerType
    ) {
        notificationDao.insert(new Notification(
                notificationDao.getMaxNotificationId() + 1L,
                targetUserId,
                relatedPhotoId,
                message,
                triggerType,
                false,
                false,
                System.currentTimeMillis()
        ));

        if (targetUserId == appSessionManager.getCurrentUserId()) {
            dispatchPendingSystemNotifications();
        }
    }

    private void seedDatabaseIfNeeded() {
        if (photoMetadataDao.countAll() > 0) {
            return;
        }

        userDao.insertAll(Arrays.asList(
                new User(1L, "kylian", "kylian@traveling.app", PasswordUtils.hash("kylian123"), false),
                new User(2L, "esteban", "esteban@traveling.app", PasswordUtils.hash("esteban123"), false),
                new User(3L, "maya", "maya@traveling.app", PasswordUtils.hash("maya123"), false),
                new User(4L, "voyage_anonyme", "", "", true)
        ));

        appPreferencesDao.insert(new AppPreferences(1L, 1L, AppTheme.SYSTEM, "fr", true));
        appPreferencesDao.insert(new AppPreferences(2L, 2L, AppTheme.SYSTEM, "fr", true));
        appPreferencesDao.insert(new AppPreferences(3L, 3L, AppTheme.SYSTEM, "fr", true));

        travelGroupDao.insertAll(Arrays.asList(
                new TravelGroup(401L, "Escapades urbaines", 1L, false),
                new TravelGroup(402L, "Kyoto secrets", 2L, true),
                new TravelGroup(403L, "Food trips Europe", 3L, false)
        ));

        groupMembershipDao.insertAll(Arrays.asList(
                new GroupMembership(401L, 1L),
                new GroupMembership(401L, 2L),
                new GroupMembership(402L, 2L),
                new GroupMembership(402L, 3L),
                new GroupMembership(403L, 3L),
                new GroupMembership(403L, 1L)
        ));

        locationDao.insertAll(Arrays.asList(
                new Location(101L, 48.8584, 2.2945, "Champ de Mars", "Paris", "France"),
                new Location(102L, 35.0116, 135.7681, "Quartier de Gion", "Kyoto", "Japon"),
                new Location(103L, 41.8902, 12.4922, "Piazza del Colosseo", "Rome", "Italie"),
                new Location(104L, 41.3851, 2.1734, "Barri Gotic", "Barcelone", "Espagne"),
                new Location(105L, 48.8867, 2.3431, "Montmartre", "Paris", "France"),
                new Location(106L, 34.9671, 135.7727, "Fushimi Inari", "Kyoto", "Japon"),
                new Location(107L, 41.8894, 12.4709, "Trastevere", "Rome", "Italie"),
                new Location(108L, 41.3765, 2.1921, "Barceloneta", "Barcelone", "Espagne")
        ));

        mediaDao.insertAll(Arrays.asList(
                new Media(201L, 2L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(202L, 3L, "travel_japon", MediaType.PHOTO, "travel_japon"),
                new Media(203L, 2L, "travel_colosseum", MediaType.PHOTO, "travel_colosseum"),
                new Media(204L, 1L, "travel_barcelone", MediaType.PHOTO, "travel_barcelone"),
                new Media(205L, 1L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(206L, 2L, "travel_japon", MediaType.PHOTO, "travel_japon"),
                new Media(207L, 3L, "travel_colosseum", MediaType.PHOTO, "travel_colosseum"),
                new Media(208L, 1L, "travel_barcelone", MediaType.PHOTO, "travel_barcelone")
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
                        401L,
                        null,
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
                        402L,
                        null,
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
                        403L,
                        null,
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
                        null,
                        null,
                        Arrays.asList("mediterranean", "sea", "tapas"),
                        PlaceType.RESTAURANT
                ),
                new PhotoMetadata(
                        305L,
                        1L,
                        "Pause café à Montmartre",
                        "Une matinée tranquille entre ruelles pavées, terrasses discrètes et vue dégagée sur les toits de Paris.",
                        1776719400000L,
                        105L,
                        205L,
                        401L,
                        null,
                        Arrays.asList("paris", "montmartre", "coffee"),
                        PlaceType.STREET
                ),
                new PhotoMetadata(
                        306L,
                        2L,
                        "Escalier rouge à Kyoto",
                        "Un passage marquant au milieu des torii, avec une atmosphère calme et un rythme plus lent que dans le centre-ville.",
                        1773282600000L,
                        106L,
                        206L,
                        402L,
                        null,
                        Arrays.asList("kyoto", "torii", "japan"),
                        PlaceType.NATURE
                ),
                new PhotoMetadata(
                        307L,
                        3L,
                        "Fin d'après-midi à Rome",
                        "Une halte à Trastevere après la visite des monuments, entre façades chaudes, petites places et lumière dorée.",
                        1771738200000L,
                        107L,
                        207L,
                        null,
                        null,
                        Arrays.asList("rome", "trastevere", "sunset"),
                        PlaceType.STREET
                ),
                new PhotoMetadata(
                        308L,
                        1L,
                        "Week-end à Barceloneta",
                        "Un moment simple entre promenade en bord de mer, restaurants ouverts tard et ambiance détendue de fin de journée.",
                        1770170400000L,
                        108L,
                        208L,
                        403L,
                        null,
                        Arrays.asList("barcelona", "beach", "weekend"),
                        PlaceType.RESTAURANT
                )
        ));

        commentDao.insertAll(Arrays.asList(
                new Comment(1000L, 301L, 1L, "La lumière est incroyable, on sent vraiment l'ambiance du matin.", "", 1775948400000L),
                new Comment(1001L, 301L, 3L, "Super point de vue, je vais l'ajouter à mon prochain city trip.", "", 1775950200000L),
                new Comment(1002L, 302L, 2L, "Le contraste entre les ruelles et les arbres en fleurs marche vraiment bien.", "", 1772589900000L),
                new Comment(1003L, 304L, 2L, "On imagine déjà la fin de journée au bord de l'eau.", "", 1769505300000L),
                new Comment(1004L, 305L, 2L, "Le cadre donne vraiment envie de prendre son temps.", "", 1776722400000L),
                new Comment(1005L, 305L, 3L, "Très belle série, le lieu colle bien à l'esprit du post.", "", 1776723900000L),
                new Comment(1006L, 306L, 1L, "Le rouge ressort super bien, ça donne beaucoup de présence à la photo.", "", 1773285000000L),
                new Comment(1007L, 307L, 2L, "J'aime bien l'ambiance plus locale que touristique.", "", 1771740900000L),
                new Comment(1008L, 308L, 3L, "On sent vraiment le week-end détente.", "", 1770172800000L)
        ));

        socialInteractionDao.insertAll(Arrays.asList(
                new SocialInteraction(1000L, 1L, 301L, SocialInteractionType.LIKE),
                new SocialInteraction(1001L, 2L, 301L, SocialInteractionType.LIKE),
                new SocialInteraction(1002L, 3L, 301L, SocialInteractionType.LIKE),
                new SocialInteraction(1003L, 1L, 304L, SocialInteractionType.LIKE),
                new SocialInteraction(1004L, 3L, 304L, SocialInteractionType.LIKE),
                new SocialInteraction(1005L, 2L, 305L, SocialInteractionType.LIKE),
                new SocialInteraction(1006L, 3L, 305L, SocialInteractionType.LIKE),
                new SocialInteraction(1007L, 1L, 306L, SocialInteractionType.LIKE),
                new SocialInteraction(1008L, 3L, 306L, SocialInteractionType.LIKE),
                new SocialInteraction(1009L, 1L, 307L, SocialInteractionType.LIKE),
                new SocialInteraction(1010L, 2L, 307L, SocialInteractionType.LIKE),
                new SocialInteraction(1011L, 2L, 308L, SocialInteractionType.LIKE),
                new SocialInteraction(1012L, 3L, 308L, SocialInteractionType.LIKE)
        ));
    }

    private AppPreferences getOrCreatePreferencesForUser(long userId) {
        AppPreferences preferences = appPreferencesDao.getByUserId(userId);
        if (preferences != null) {
            return preferences;
        }

        AppPreferences defaultPreferences = new AppPreferences(userId, userId, AppTheme.SYSTEM, "fr", true);
        appPreferencesDao.insert(defaultPreferences);
        return defaultPreferences;
    }

    private boolean canAccessPhotoMetadata(PhotoMetadata photoMetadata) {
        if (photoMetadata == null) {
            return false;
        }
        if (photoMetadata.getGroupId() == null) {
            return true;
        }

        TravelGroup group = travelGroupDao.getById(photoMetadata.getGroupId());
        return canAccessGroup(group);
    }

    private boolean canAccessGroup(TravelGroup group) {
        if (group == null) {
            return false;
        }
        if (!group.isPrivate()) {
            return true;
        }
        return isCurrentUserMemberOfGroup(group.getGroupId());
    }

    private String getStringResource(int resId) {
        return appContext.getString(resId);
    }

    public static class ProfileStats {
        private final int publicationsCount;
        private final int commentsCount;
        private final int likesReceivedCount;

        public ProfileStats(int publicationsCount, int commentsCount, int likesReceivedCount) {
            this.publicationsCount = publicationsCount;
            this.commentsCount = commentsCount;
            this.likesReceivedCount = likesReceivedCount;
        }

        public int getPublicationsCount() {
            return publicationsCount;
        }

        public int getCommentsCount() {
            return commentsCount;
        }

        public int getLikesReceivedCount() {
            return likesReceivedCount;
        }
    }
}
