package com.kcorteel.travel_esteban_kylian.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Notification;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private final TravelShareRepository repository;
    private final OnNotificationClickListener onNotificationClickListener;
    private final List<Notification> notifications;
    private final DateFormat dateFormat;

    public NotificationAdapter(
            TravelShareRepository repository,
            OnNotificationClickListener onNotificationClickListener
    ) {
        this.repository = repository;
        this.onNotificationClickListener = onNotificationClickListener;
        this.notifications = new ArrayList<>();
        this.dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT,
                repository.getCurrentLocale()
        );
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void submitNotifications(List<Notification> items) {
        notifications.clear();
        notifications.addAll(items);
        notifyDataSetChanged();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final TextView typeTextView;
        private final TextView messageTextView;
        private final TextView dateTextView;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.tvNotificationType);
            messageTextView = itemView.findViewById(R.id.tvNotificationMessage);
            dateTextView = itemView.findViewById(R.id.tvNotificationDate);
        }

        void bind(Notification notification) {
            typeTextView.setText(repository.getNotificationTypeLabel(notification));
            messageTextView.setText(notification.getMessage());
            dateTextView.setText(dateFormat.format(new Date(notification.getCreatedAt())));

            itemView.setAlpha(notification.isRead() ? 0.72f : 1f);
            itemView.setOnClickListener(v -> {
                if (onNotificationClickListener != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    onNotificationClickListener.onNotificationClick(notification);
                }
            });
        }
    }
}
