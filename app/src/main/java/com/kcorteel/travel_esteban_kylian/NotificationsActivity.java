package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.travelshare.adapter.NotificationAdapter;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Notification;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private TextView emptyStateTextView;
    private NotificationAdapter notificationAdapter;
    private TravelShareRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TravelShareRepository.getInstance(this);
        repository.applyCurrentUserThemePreference();
        setContentView(R.layout.activity_notifications);

        notificationsRecyclerView = findViewById(R.id.rvNotifications);
        emptyStateTextView = findViewById(R.id.tvNotificationsEmptyState);

        notificationAdapter = new NotificationAdapter(repository, this::openRelatedPost);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(notificationAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        repository.markCurrentUserNotificationsAsRead();
        bindNotifications();
    }

    private void bindNotifications() {
        List<Notification> notifications = repository.getNotificationsForCurrentUser();
        notificationAdapter.submitNotifications(notifications);
        emptyStateTextView.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        notificationsRecyclerView.setVisibility(notifications.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void openRelatedPost(Notification notification) {
        if (notification == null || notification.getRelatedPhotoId() <= 0L) {
            return;
        }

        Intent intent = new Intent(this, TravelShareDetailActivity.class);
        intent.putExtra(TravelShareDetailActivity.EXTRA_PHOTO_ID, notification.getRelatedPhotoId());
        startActivity(intent);
    }
}
