package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

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

        photoPostAdapter = new PhotoPostAdapter(createMockPosts(), this::openPhotoDetails);

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

    private void openPhotoDetails(PhotoPost photoPost) {
        Intent intent = new Intent(this, TravelShareDetailActivity.class);
        intent.putExtra(TravelShareDetailActivity.EXTRA_PHOTO_POST, photoPost);
        startActivity(intent);
    }

    private List<PhotoPost> createMockPosts() {
        List<PhotoPost> posts = new ArrayList<>();

        posts.add(new PhotoPost(
                "Balade au lever du soleil",
                "Paris",
                "12 avril 2026",
                "Une promenade matinale le long de la Seine avec une vue magnifique sur la Tour Eiffel.",
                "Descendre à Bir-Hakeim puis marcher dix minutes vers les quais pour rejoindre le point de vue.",
                R.drawable.img_mock_paris,
                48.8584,
                2.2945,
                true
        ));

        posts.add(new PhotoPost(
                "Temples et cerisiers",
                "Kyoto",
                "4 mars 2026",
                "Une journée entre sanctuaires, ruelles traditionnelles et fleurs de cerisier en pleine saison.",
                "Prendre le bus local jusqu'à Gion puis poursuivre à pied jusqu'aux temples et jardins proches.",
                R.drawable.img_mock_kyoto,
                35.0116,
                135.7681,
                false
        ));

        posts.add(new PhotoPost(
                "Escapade historique",
                "Rome",
                "18 février 2026",
                "Découverte du Colisée, des places animées et d'une cuisine italienne pleine de saveurs.",
                "Sortir au métro Colosseo puis rejoindre l'entrée principale en suivant l'esplanade piétonne.",
                R.drawable.img_mock_rome,
                41.8902,
                12.4922,
                false
        ));

        posts.add(new PhotoPost(
                "Ambiance méditerranéenne",
                "Barcelone",
                "27 janvier 2026",
                "Entre architecture colorée, bord de mer et tapas partagées au coucher du soleil.",
                "Descendre à Jaume I, remonter vers le quartier gothique puis rejoindre la mer à pied.",
                R.drawable.img_mock_barcelona,
                41.3851,
                2.1734,
                true
        ));

        return posts;
    }
}
