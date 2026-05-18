package com.kcorteel.travel_esteban_kylian.travelshare.repository;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

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
import com.kcorteel.travel_esteban_kylian.travelshare.model.ActivityType;
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
import com.kcorteel.travel_esteban_kylian.travelshare.util.TravelShareLocaleManager;

import java.io.File;
import java.text.Normalizer;
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
            return getStringResource(R.string.profile_update_requires_login);
        }

        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedAvatarUri = avatarUri == null
                ? currentUser.getAvatarUri()
                : avatarUri.trim();

        if (normalizedUsername.isEmpty() || normalizedEmail.isEmpty()) {
            return getStringResource(R.string.profile_update_missing_identity_error);
        }

        User existingByUsername = userDao.getByUsername(normalizedUsername);
        if (existingByUsername != null && existingByUsername.getUserId() != currentUser.getUserId()) {
            return getStringResource(R.string.auth_username_exists_error);
        }

        User existingByEmail = userDao.getByEmail(normalizedEmail);
        if (existingByEmail != null && existingByEmail.getUserId() != currentUser.getUserId()) {
            return getStringResource(R.string.auth_email_exists_error);
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

    public void applyCurrentUserLanguagePreference() {
        TravelShareLocaleManager.applyLanguage(getCurrentUserPreferences().getLanguage());
    }

    public void applyCurrentUserDisplayPreferences() {
        applyCurrentUserLanguagePreference();
        applyCurrentUserThemePreference();
    }

    public Locale getCurrentLocale() {
        return TravelShareLocaleManager.resolveLocale(appContext);
    }

    public String getCurrentLanguageTag() {
        return getCurrentLocale().toLanguageTag();
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
                0d,
                "09:00",
                "18:00",
                mapPlaceTypeToActivityType(placeType),
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
            return getStringResource(R.string.travelshare_group_login_required);
        }

        String normalizedName = groupName == null ? "" : groupName.trim();
        if (normalizedName.isEmpty()) {
            return getStringResource(R.string.travelshare_group_name_required);
        }

        for (TravelGroup existingGroup : travelGroupDao.getAll()) {
            if (existingGroup.getGroupName().equalsIgnoreCase(normalizedName)) {
                return getStringResource(R.string.travelshare_group_exists_error);
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

    public boolean isCurrentUserAuthorOfPhoto(long photoId) {
        PhotoMetadata photoMetadata = getPhotoMetadataById(photoId);
        return photoMetadata != null
                && !isCurrentUserAnonymous()
                && photoMetadata.getAuthorId() == appSessionManager.getCurrentUserId();
    }

    public boolean deletePhotoMetadata(long photoId) {
        PhotoMetadata photoMetadata = photoMetadataDao.getById(photoId);
        if (photoMetadata == null || !isCurrentUserAuthorOfPhoto(photoId)) {
            return false;
        }

        deleteLocalVoiceNoteIfPossible(photoMetadata.getAudioNoteUrl());
        commentDao.deleteByPhotoId(photoId);
        socialInteractionDao.deleteByPhotoId(photoId);
        notificationDao.deleteByRelatedPhotoId(photoId);
        photoMetadataDao.deleteById(photoId);
        mediaDao.deleteById(photoMetadata.getMediaId());
        locationDao.deleteById(photoMetadata.getLocationId());
        return true;
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
            imageView.setImageTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(appContext, R.color.app_text_primary)
            ));
            return;
        }

        String avatarUri = user.getAvatarUri().trim();
        if (avatarUri.startsWith("content://") || avatarUri.startsWith("file://")) {
            imageView.setImageURI(Uri.parse(avatarUri));
            imageView.setImageTintList(null);
            return;
        }

        imageView.setImageResource(android.R.drawable.ic_menu_myplaces);
        imageView.setImageTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(appContext, R.color.app_text_primary)
        ));
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
            return getStringResource(R.string.travelshare_route_advice_missing_coordinates);
        }

        return appContext.getString(
                R.string.travelshare_route_advice_format,
                location.getAddress(),
                location.getCity()
        );
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

    public List<PhotoMetadata> getGreedyTravelPath(
            String budget,
            String duration,
            boolean culture,
            boolean leisure,
            boolean food,
            String effort,
            String pathType
    ) {
        List<PhotoMetadata> allPois = photoMetadataDao.getAll();
        List<PhotoMetadata> parisPois = new ArrayList<>();
        for (PhotoMetadata poi : allPois) {
            Location location = getLocationById(poi.getLocationId());
            if (location != null && "paris".equalsIgnoreCase(location.getCity())) {
                parisPois.add(poi);
            }
        }

        if (parisPois.isEmpty()) {
            return Collections.emptyList();
        }

        List<PhotoMetadata> rankedPois = new ArrayList<>(parisPois);
        rankedPois.sort((a, b) -> Integer.compare(
                scorePoi(b, culture, leisure, food, effort, pathType, budget, duration),
                scorePoi(a, culture, leisure, food, effort, pathType, budget, duration)
        ));

        List<PhotoMetadata> selected = new ArrayList<>();
        selected.add(rankedPois.get(0)); // Always keep at least one POI

        double preferredHours = parsePreferredHours(duration);
        double preferredBudget = parsePreferredBudget(budget);
        String mode = normalizeMode(pathType);
        int effortPoiCap = resolveEffortPoiCap(effort);

        for (int i = 1; i < rankedPois.size(); i++) {
            if (effortPoiCap > 0 && selected.size() >= effortPoiCap) {
                break;
            }

            PhotoMetadata candidate = rankedPois.get(i);
            selected.add(candidate);

            double estimatedHours = computeEstimatedTotalDurationHours(selected);
            double totalBudget = computeTotalActivitiesPrice(selected);

            if ("confort".equals(mode)) {
                if (preferredHours > 0 && estimatedHours > preferredHours) {
                    if (selected.size() > 1) {
                        selected.remove(selected.size() - 1);
                    }
                    break;
                }
            } else if ("economique".equals(mode) || "economical".equals(mode)) {
                if (preferredBudget >= 0 && totalBudget > preferredBudget) {
                    if (selected.size() > 1) {
                        selected.remove(selected.size() - 1);
                    }
                    break;
                }
            } else { // Balanced mode: do not exceed both time and budget
                boolean exceedsTime = preferredHours > 0 && estimatedHours > preferredHours;
                boolean exceedsBudget = preferredBudget >= 0 && totalBudget > preferredBudget;
                if (exceedsTime || exceedsBudget) {
                    if (selected.size() > 1) {
                        selected.remove(selected.size() - 1);
                    }
                    break;
                }
            }
        }

        return selected;
    }

    private double parsePreferredHours(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return -1d;
        }
        try {
            return Double.parseDouble(duration.trim());
        } catch (NumberFormatException ignored) {
            return -1d;
        }
    }

    private double parsePreferredBudget(String budget) {
        if (budget == null || budget.trim().isEmpty()) {
            return -1d;
        }
        try {
            return Double.parseDouble(budget.trim());
        } catch (NumberFormatException ignored) {
            return -1d;
        }
    }

    private String normalizeMode(String mode) {
        if (mode == null) {
            return "";
        }
        String normalized = Normalizer.normalize(mode, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.getDefault())
                .trim();
        if (normalized.contains("equilibre")) return "equilibre";
        if (normalized.contains("econom")) return "economique";
        if (normalized.contains("confort")) return "confort";
        return normalized;
    }

    private int resolveEffortPoiCap(String effort) {
        if (effort == null) {
            return -1;
        }
        String normalized = Normalizer.normalize(effort, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.getDefault())
                .trim();

        if (normalized.contains("faible") || normalized.contains("light")) {
            return 2;
        }
        if (normalized.contains("moyen") || normalized.contains("medium")) {
            return 5;
        }
        if (normalized.contains("eleve") || normalized.contains("high")) {
            return -1;
        }
        return -1;
    }

    private double computeEstimatedTotalDurationHours(List<PhotoMetadata> pois) {
        return computeTotalActivitiesDurationHours(pois) + computeTotalTravelDurationHours(pois);
    }

    private double estimateVisitDurationHours(PhotoMetadata poi) {
        if (poi == null) return 0d;
        switch (poi.getActivityType()) {
            case CULTURAL:
                return 2.0d;
            case FOOD:
                return 1.25d;
            case LEISURE:
            default:
                return 1.5d;
        }
    }

    public double estimateActivityDurationHours(PhotoMetadata poi) {
        return estimateVisitDurationHours(poi);
    }

    public double computeTotalActivitiesDurationHours(List<PhotoMetadata> pois) {
        if (pois == null || pois.isEmpty()) {
            return 0d;
        }
        double total = 0d;
        for (PhotoMetadata poi : pois) {
            total += estimateVisitDurationHours(poi);
        }
        return total;
    }

    public double computeTotalActivitiesPrice(List<PhotoMetadata> pois) {
        if (pois == null || pois.isEmpty()) {
            return 0d;
        }
        double total = 0d;
        for (PhotoMetadata poi : pois) {
            total += Math.max(0d, poi.getPrice());
        }
        return total;
    }

    public double estimateTravelDurationHoursBetween(PhotoMetadata fromPoi, PhotoMetadata toPoi) {
        if (fromPoi == null || toPoi == null) {
            return 0d;
        }
        return estimateTravelHoursBetween(fromPoi, toPoi);
    }

    public double computeTotalTravelDurationHours(List<PhotoMetadata> pois) {
        if (pois == null || pois.size() < 2) {
            return 0d;
        }

        double total = 0d;
        for (int i = 0; i < pois.size() - 1; i++) {
            total += estimateTravelHoursBetween(pois.get(i), pois.get(i + 1));
        }
        return total;
    }

    private double estimateTravelHoursBetween(PhotoMetadata fromPoi, PhotoMetadata toPoi) {
        Location from = getLocationById(fromPoi.getLocationId());
        Location to = getLocationById(toPoi.getLocationId());
        if (from == null || to == null) {
            return 0d;
        }

        double distanceKm = haversineKm(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
        double avgCitySpeedKmH = 5.0d;
        return distanceKm / avgCitySpeedKmH;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0d;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private int scorePoi(
            PhotoMetadata poi,
            boolean culture,
            boolean leisure,
            boolean food,
            String effort,
            String pathType,
            String budget,
            String duration
    ) {
        int score = 0;
        ActivityType activityType = poi.getActivityType();
        Location location = getLocationById(poi.getLocationId());
        int internalPriceLevel = getInternalPriceLevel(poi);
        int internalLocalizationScore = getInternalLocalizationScore(location);

        if (culture && activityType == ActivityType.CULTURAL) score += 5;
        if (leisure && activityType == ActivityType.LEISURE) score += 4;
        if (food && activityType == ActivityType.FOOD) score += 5;

        String normalizedEffort = effort == null ? "" : effort.toLowerCase(Locale.getDefault());
        if ("faible".equals(normalizedEffort) && internalLocalizationScore >= 2) score += 2;
        if ("moyen".equals(normalizedEffort) && internalLocalizationScore >= 1) score += 2;
        if ("élevé".equals(normalizedEffort) && internalLocalizationScore <= 1) score += 2;

        String normalizedType = pathType == null ? "" : pathType.toLowerCase(Locale.getDefault());
        if ("économique".equals(normalizedType) && internalPriceLevel == 1) score += 3;
        if ("équilibré".equals(normalizedType)) score += 1;
        if ("confort".equals(normalizedType) && internalPriceLevel >= 2) score += 2;

        String normalizedBudget = budget == null ? "" : budget.trim();
        if (!normalizedBudget.isEmpty()) {
            try {
                double budgetValue = Double.parseDouble(normalizedBudget);
                if (budgetValue < 60 && internalPriceLevel <= 1) score += 3;
                if (budgetValue >= 60 && budgetValue < 150 && internalPriceLevel == 2) score += 3;
                if (budgetValue >= 150 && internalPriceLevel >= 3) score += 3;
            } catch (NumberFormatException ignored) {
            }
        }

        String normalizedDuration = duration == null ? "" : duration.trim();
        if (!normalizedDuration.isEmpty()) {
            try {
                int durationValue = Integer.parseInt(normalizedDuration);
                if (durationValue <= 3 && internalLocalizationScore >= 2) score += 2;
                if (durationValue > 3 && durationValue <= 6 && internalLocalizationScore >= 1) score += 1;
                if (durationValue > 6) score += 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return score;
    }

    private int getInternalPriceLevel(PhotoMetadata poi) {
        double price = poi.getPrice();
        if (price < 25d) return 1;
        if (price < 80d) return 2;
        return 3;
    }

    private ActivityType mapPlaceTypeToActivityType(PlaceType placeType) {
        if (placeType == PlaceType.MUSEUM) return ActivityType.CULTURAL;
        if (placeType == PlaceType.RESTAURANT) return ActivityType.FOOD;
        return ActivityType.LEISURE;
    }

    private int getInternalLocalizationScore(Location location) {
        if (location == null) return 0;
        String address = location.getAddress() == null ? "" : location.getAddress().toLowerCase(Locale.getDefault());
        if (address.contains("champs") || address.contains("rivoli") || address.contains("notre-dame")) return 3;
        if (address.contains("montmartre") || address.contains("latin")) return 2;
        return 1;
    }

    public String getNotificationTypeLabel(Notification notification) {
        if (notification == null) {
            return "";
        }

        switch (notification.getTriggerType()) {
            case NEW_POST_IN_GROUP:
                return getStringResource(R.string.notifications_type_group_post);
            case LIKE_ON_PHOTO:
                return getStringResource(R.string.notifications_type_like);
            case COMMENT_ON_PHOTO:
                return getStringResource(R.string.notifications_type_comment);
            case NEW_POST_BY_USER:
            case NEW_POST_IN_LOCATION:
            case NEW_TAG_MATCH:
            default:
                return getStringResource(R.string.notifications_type_new_post);
        }
    }

    public String getPlaceTypeLabel(PlaceType placeType) {
        if (placeType == null) {
            return getStringResource(R.string.travelshare_place_type_other);
        }

        switch (placeType) {
            case NATURE:
                return getStringResource(R.string.travelshare_place_type_nature);
            case MUSEUM:
                return getStringResource(R.string.travelshare_place_type_museum);
            case STREET:
                return getStringResource(R.string.travelshare_place_type_street);
            case SHOP:
                return getStringResource(R.string.travelshare_place_type_shop);
            case RESTAURANT:
                return getStringResource(R.string.travelshare_place_type_restaurant);
            case OTHER:
            default:
                return getStringResource(R.string.travelshare_place_type_other);
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
                    appContext.getString(
                            R.string.notifications_message_new_post_by_user,
                            author.getUsername(),
                            photoMetadata.getTitle()
                    ),
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
                    appContext.getString(
                            R.string.notifications_message_new_post_in_group,
                            author.getUsername(),
                            group.getGroupName()
                    ),
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
                appContext.getString(
                        R.string.notifications_message_comment_on_photo,
                        actor.getUsername(),
                        commentText
                ),
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
                appContext.getString(
                        R.string.notifications_message_like_on_photo,
                        actor.getUsername(),
                        photoMetadata.getTitle()
                ),
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

    private void deleteLocalVoiceNoteIfPossible(String voiceNotePath) {
        if (TextUtils.isEmpty(voiceNotePath)) {
            return;
        }

        String normalizedPath = voiceNotePath.trim();
        if (normalizedPath.startsWith("content://") || normalizedPath.startsWith("file://")) {
            return;
        }

        try {
            new File(normalizedPath).delete();
        } catch (Exception ignored) {
            // Best effort only.
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
                new TravelGroup(402L, "Paris culture privée", 2L, true),
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
                new Location(101L, 48.85837, 2.294481, "Champ de Mars", "Paris", "France"),
                new Location(102L, 48.860611, 2.337644, "Rue de Rivoli", "Paris", "France"),
                new Location(103L, 48.852968, 2.349902, "Île de la Cité", "Paris", "France"),
                new Location(104L, 48.886529, 2.340775, "Place du Tertre, Montmartre", "Paris", "France"),
                new Location(105L, 48.884067, 2.338110, "Place des Abbesses, Montmartre", "Paris", "France"),
                new Location(106L, 48.846222, 2.337160, "Jardin du Luxembourg", "Paris", "France"),
                new Location(107L, 48.869798, 2.307770, "Avenue des Champs-Élysées", "Paris", "France"),
                new Location(108L, 48.865633, 2.321236, "Place de la Concorde", "Paris", "France"),
                new Location(109L, 48.858370, 2.294481, "Tour Eiffel, 2e étage", "Paris", "France")
        ));

        mediaDao.insertAll(Arrays.asList(
                new Media(201L, 2L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(202L, 3L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(203L, 2L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(204L, 1L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(205L, 1L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(206L, 2L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(207L, 3L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(208L, 1L, "travel_paris", MediaType.PHOTO, "travel_paris"),
                new Media(209L, 2L, "travel_paris", MediaType.PHOTO, "travel_paris")
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
                        0d,
                        "06:00",
                        "22:00",
                        ActivityType.LEISURE,
                        401L,
                        null,
                        Arrays.asList("sunrise", "eiffel", "seine"),
                        PlaceType.STREET
                ),
                new PhotoMetadata(
                        302L,
                        3L,
                        "Musée du Louvre",
                        "Une visite culturelle autour des galeries du Louvre et des façades historiques du centre de Paris.",
                        1772586000000L,
                        102L,
                        202L,
                        35d,
                        "09:00",
                        "18:00",
                        ActivityType.CULTURAL,
                        402L,
                        null,
                        Arrays.asList("louvre", "museum", "paris"),
                        PlaceType.MUSEUM
                ),
                new PhotoMetadata(
                        303L,
                        2L,
                        "Balade sur l'île de la Cité",
                        "Découverte du coeur historique de Paris, entre ponts, quais et architecture emblématique.",
                        1771138800000L,
                        103L,
                        203L,
                        20d,
                        "08:30",
                        "19:00",
                        ActivityType.CULTURAL,
                        402L,
                        null,
                        Arrays.asList("cite", "history", "paris"),
                        PlaceType.MUSEUM
                ),
                new PhotoMetadata(
                        304L,
                        1L,
                        "Saveurs de Montmartre",
                        "Un parcours gourmand avec haltes dans des adresses parisiennes conviviales du quartier Montmartre.",
                        1769500800000L,
                        104L,
                        204L,
                        65d,
                        "11:30",
                        "23:00",
                        ActivityType.FOOD,
                        403L,
                        null,
                        Arrays.asList("montmartre", "food", "paris"),
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
                        12d,
                        "07:00",
                        "20:00",
                        ActivityType.FOOD,
                        401L,
                        null,
                        Arrays.asList("paris", "montmartre", "coffee"),
                        PlaceType.STREET
                ),
                new PhotoMetadata(
                        306L,
                        2L,
                        "Pause verte au Luxembourg",
                        "Un itinéraire détente entre allées arborées et fontaines du Jardin du Luxembourg.",
                        1773282600000L,
                        106L,
                        206L,
                        0d,
                        "08:00",
                        "21:00",
                        ActivityType.LEISURE,
                        null,
                        null,
                        Arrays.asList("luxembourg", "garden", "paris"),
                        PlaceType.NATURE
                ),
                new PhotoMetadata(
                        307L,
                        3L,
                        "Soirée sur les Champs-Élysées",
                        "Un parcours urbain vivant en fin de journée, entre vitrines, places et ambiance parisienne.",
                        1771738200000L,
                        107L,
                        207L,
                        0d,
                        "10:00",
                        "22:00",
                        ActivityType.LEISURE,
                        null,
                        null,
                        Arrays.asList("champs", "street", "paris"),
                        PlaceType.STREET
                ),
                new PhotoMetadata(
                        308L,
                        1L,
                        "Dîner près de la Concorde",
                        "Une fin de parcours confortable avec une table parisienne proche de la place de la Concorde.",
                        1770170400000L,
                        108L,
                        208L,
                        85d,
                        "12:00",
                        "23:30",
                        ActivityType.FOOD,
                        403L,
                        null,
                        Arrays.asList("concorde", "restaurant", "paris"),
                        PlaceType.RESTAURANT
                ),
                new PhotoMetadata(
                        309L,
                        2L,
                        "Dîner gastronomique Tour Eiffel",
                        "Expérience gastronomique premium avec vue panoramique sur Paris depuis la Tour Eiffel.",
                        1777903200000L,
                        109L,
                        209L,
                        320d,
                        "12:00",
                        "23:00",
                        ActivityType.FOOD,
                        null,
                        null,
                        Arrays.asList("jules-verne", "fine-dining", "eiffel"),
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
                new Comment(1008L, 308L, 3L, "On sent vraiment le week-end détente.", "", 1770172800000L),
                new Comment(1009L, 309L, 3L, "Une expérience haut de gamme, parfaite pour un parcours confort.", "", 1777906800000L)
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
                new SocialInteraction(1012L, 3L, 308L, SocialInteractionType.LIKE),
                new SocialInteraction(1013L, 1L, 309L, SocialInteractionType.LIKE),
                new SocialInteraction(1014L, 3L, 309L, SocialInteractionType.LIKE)
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
