package com.kcorteel.travel_esteban_kylian;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SavedTravelAdapter extends ArrayAdapter<String> {

    private final List<String> fileNames;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public SavedTravelAdapter(Context context, List<String> fileNames, OnDeleteClickListener onDeleteClickListener) {
        super(context, R.layout.item_saved_travel, fileNames);
        this.fileNames = fileNames;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_saved_travel, parent, false);
        }

        TextView fileNameTextView = convertView.findViewById(R.id.fileNameTextView);
        ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);

        fileNameTextView.setText(fileNames.get(position));
        deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position);
            }
        });

        return convertView;
    }
}
