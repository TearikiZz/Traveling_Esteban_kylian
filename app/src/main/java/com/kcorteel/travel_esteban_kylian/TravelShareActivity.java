package com.kcorteel.travel_esteban_kylian;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.travelshare.adapter.PhotoPostAdapter;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoPost;

import java.util.ArrayList;
import java.util.List;

public class TravelShareActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView postsRecyclerView;
    private PhotoPostAdapter photoPostAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_share);

        searchEditText = findViewById(R.id.etSearchPost);
        postsRecyclerView = findViewById(R.id.rvPhotoPosts);

        photoPostAdapter = new PhotoPostAdapter(createMockPosts(),
                photoPost -> Toast.makeText(
                        TravelShareActivity.this,
                        photoPost.getTitle(),
                        Toast.LENGTH_SHORT
                ).show());

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setHasFixedSize(true);
        postsRecyclerView.setAdapter(photoPostAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                photoPostAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op
            }
        });
    }

    private List<PhotoPost> createMockPosts() {
        List<PhotoPost> posts = new ArrayList<>();

        posts.add(new PhotoPost(
                "Balade au lever du soleil",
                "Paris",
                "12 avril 2026",
                "Une promenade matinale le long de la Seine avec une vue magnifique sur la Tour Eiffel."
        ));

        posts.add(new PhotoPost(
                "Temples et cerisiers",
                "Kyoto",
                "4 mars 2026",
                "Une journée entre sanctuaires, ruelles traditionnelles et fleurs de cerisier en pleine saison."
        ));

        posts.add(new PhotoPost(
                "Escapade historique",
                "Rome",
                "18 février 2026",
                "Découverte du Colisée, des places animées et d'une cuisine italienne pleine de saveurs."
        ));

        posts.add(new PhotoPost(
                "Ambiance méditerranéenne",
                "Barcelone",
                "27 janvier 2026",
                "Entre architecture colorée, bord de mer et tapas partagées au coucher du soleil."
        ));

        return posts;
    }
}
