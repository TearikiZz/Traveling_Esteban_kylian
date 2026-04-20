package com.kcorteel.travel_esteban_kylian.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Comment;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final TravelShareRepository repository;
    private final List<Comment> commentList;
    private final DateFormat dateFormat;

    public CommentAdapter(TravelShareRepository repository) {
        this.repository = repository;
        this.commentList = new ArrayList<>();
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(commentList.get(position));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void submitComments(List<Comment> comments) {
        commentList.clear();
        commentList.addAll(comments);
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {

        private final TextView authorTextView;
        private final TextView dateTextView;
        private final TextView textTextView;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tvCommentAuthor);
            dateTextView = itemView.findViewById(R.id.tvCommentDate);
            textTextView = itemView.findViewById(R.id.tvCommentText);
        }

        void bind(Comment comment) {
            User author = repository.getUserById(comment.getUserId());
            authorTextView.setText(author != null ? author.getUsername() : itemView.getContext().getString(R.string.travelshare_unknown_user));
            dateTextView.setText(dateFormat.format(new Date(comment.getCreatedAt())));
            textTextView.setText(comment.getText());
        }
    }
}
