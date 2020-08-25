package com.trashcanlocaion.toyou;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;

public class LoadViewActivity extends AppCompatActivity {
    private String imageStoragePath = "korea/seoul/gangnamgu/";
    private Intent intent;
    private String locationName;
    private String locationDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_view);
        ImageView imageView = (ImageView) findViewById(R.id.loadView);
        TextView locationTextView = (TextView) findViewById(R.id.location_text);

        intent = getIntent();

        locationName = intent.getExtras().getString("locationName");
        locationDetails = intent.getExtras().getString("locationDetails");

//        StorageReference imageRef = MainActivity.storageReference.child("seoul/gwanak-gu/4번출구.png");
        String imgFullPath = imageStoragePath + locationName +".png";
        StorageReference imageRef = MainActivity.storageReference.child(imgFullPath);

        locationTextView.setText(locationDetails);

        // GlideApp 객체는 AppGlideModule상속한 GlideModule class를 최초 빌드 시켜야 사용 가능하다.
        GlideApp.with(this)
                .load(imageRef)
                .into(imageView);
    }
}