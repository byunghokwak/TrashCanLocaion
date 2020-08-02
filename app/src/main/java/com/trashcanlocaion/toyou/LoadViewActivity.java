package com.trashcanlocaion.toyou;

import android.os.Bundle;
import android.widget.ImageView;

import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;

public class LoadViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_view);
        ImageView imageView = (ImageView) findViewById(R.id.loadView);

        StorageReference imageRef = MainActivity.storageReference.child("seoul/gwanak-gu/4번출구.png");

        // GlideApp 객체는 AppGlideModule상속한 GlideModule class를 최초 빌드 시켜야 사용 가능하다.
        GlideApp.with(this)
                .load(imageRef)
                .into(imageView);
    }
}