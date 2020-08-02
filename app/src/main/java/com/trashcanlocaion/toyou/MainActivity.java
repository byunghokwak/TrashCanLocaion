package com.trashcanlocaion.toyou;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    static String TAG = "MainActivity";
    static String filePath = "gs://trashcan-map.appspot.com";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource mLocationSource;
    static FirebaseStorage mFirestorage;
    static StorageReference storageReference;

    private NaverMap mNaverMap;
    private UiSettings uiSettings;
    private LocationOverlay mLocationOverlay;

    private List<Location> locationList;
    private List<Marker> markers;

    private FirebaseFirestore db;
    private Handler handler;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        mLocationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        db = FirebaseFirestore.getInstance();

        locationList = new ArrayList<>();
        markers = new ArrayList<>();

        handler = new Handler(Looper.getMainLooper());

        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        DocumentReference docRef = db.collection("location").document("seoul");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> locationMap = (HashMap<String, Object>)document.getData();
                    Iterator<Map.Entry<String, Object>> entries = locationMap.entrySet().iterator();
                    Log.d(TAG, "Document Snapshot data : "+document.getData());

                    while(entries.hasNext()) {
                        Map.Entry<String, Object> entry = entries.next();
                        String locationName = entry.getKey();
                        GeoPoint geoPoint = (GeoPoint) entry.getValue();
                        locationList.add(new Location(geoPoint.getLatitude(), geoPoint.getLongitude(), locationName));
                    }
                } else {
                    Log.d(TAG, "get failed with", task.getException());
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0; i<locationList.size(); ++i) {
                            Marker marker = new Marker();
                            marker.setIcon(OverlayImage.fromResource(R.mipmap.market_trashcan));
                            double latitude = locationList.get(i).getLatitude();
                            double longitude = locationList.get(i).getLongitude();
                            marker.setPosition(new LatLng(latitude, longitude));
                            marker.setWidth(Marker.SIZE_AUTO);
                            marker.setHeight(Marker.SIZE_AUTO);
                            marker.setOnClickListener(new Overlay.OnClickListener() {
                                @Override
                                public boolean onClick(@NonNull Overlay overlay) {
//                                    Toast.makeText(MainActivity.this, "마커 클릭", Toast.LENGTH_SHORT).show();
                                    // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
                                    Intent intent = new Intent(getApplicationContext(), LoadViewActivity.class);
                                    startActivityForResult(intent, 100);
                                    return true;
                                }
                            });
                            markers.add(marker);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                for(Marker marker : markers) {
                                    marker.setMap(mNaverMap);
                                    Log.d("execute", "marker");
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mFirestorage = FirebaseStorage.getInstance(filePath);
        storageReference = mFirestorage.getReference();
//        StorageReference imageRef = storageReference.child("seoul/gwanak-gu/4번출구.png");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (mLocationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!mLocationSource.isActivated()) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.None);
                Log.d("onRequestPermissions", "not activated");
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        mNaverMap = naverMap;
        uiSettings = naverMap.getUiSettings();
        mLocationOverlay = mNaverMap.getLocationOverlay();

        mNaverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true);
        mNaverMap.setLocationSource(mLocationSource);
        mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        uiSettings.setLocationButtonEnabled(true);
        uiSettings.setCompassEnabled(true);
        mLocationOverlay.setVisible(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
