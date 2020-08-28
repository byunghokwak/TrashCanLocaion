package com.trashcanlocaion.toyou;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
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
import java.util.Arrays;
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
    static DocumentReference docRef;
    static StorageReference storageReference;

    private NaverMap mNaverMap;
    private UiSettings uiSettings;
    private LocationOverlay mLocationOverlay;

    private List<Location> locationList;
    private List<Marker> markers;

    private FirebaseFirestore db;
    private Handler handler;
    private AdView adView;
    private InterstitialAd mInterstitialAd;

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
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id_for_test));

        // 전면광고 - 테스트 안 됨
//        MobileAds.initialize(this, "ca-app-pub-6539126210899032~7301018409");
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(getString(R.string.front_ad_unit_id_for_test));
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());
//        if (mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//        }

        docRef = db.collection("location").document("korea").collection("seoul").document("gangnam-gu");

        // 로컬 DB 업로드 (필요시에만 활성화하고 평소에는 안 씀)
//        uploadLocationInforamtion();

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
                        String locationIdx_Name = entry.getKey();

                        // 11+주소이름 = +로 split
                        String locationName = locationIdx_Name.split("\\+")[1];

                        ArrayList<String> dataArr = (ArrayList)entry.getValue();
                        String geoInfo[] = dataArr.get(Common.CloudFirestore.GEOMETRY).split(", ");
                        String locationDatails = dataArr.get(Common.CloudFirestore.LOCATION_DETAILS);
                        locationList.add(new Location(Double.parseDouble(geoInfo[0]), Double.parseDouble(geoInfo[1]), locationName, locationDatails));
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
                            String locationDetails = locationList.get(i).getLocationDetails();
                            String locationName = locationList.get(i).getLocationName();

                            marker.setPosition(new LatLng(latitude, longitude));
                            marker.setWidth(Marker.SIZE_AUTO);
                            marker.setHeight(Marker.SIZE_AUTO);
                            marker.setOnClickListener(new Overlay.OnClickListener() {
                                @Override
                                public boolean onClick(@NonNull Overlay overlay) {
//                                    Toast.makeText(MainActivity.this, "마커 클릭", Toast.LENGTH_SHORT).show();
                                    // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
                                    Intent intent = new Intent(getApplicationContext(), LoadViewActivity.class);
                                    intent.putExtra("locationName", locationName);
                                    intent.putExtra("locationDetails", locationDetails);
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

    public void uploadLocationInforamtion() {
        GeoUploadModule geoUploadModule = new GeoUploadModule(getResources(), docRef);
        geoUploadModule.start();
    }
}
