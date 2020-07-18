package com.trashcanlocaion.toyou;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

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


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource mLocationSource;

    private NaverMap mNaverMap;
    private UiSettings uiSettings;
    private LocationOverlay mLocationOverlay;


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

        Marker marker = new Marker();
        marker.setIcon(OverlayImage.fromResource(R.mipmap.market_trashcan));
        marker.setPosition(new LatLng(37.481286, 126.951503));
        marker.setMap(mNaverMap);
        marker.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {
                Toast.makeText(MainActivity.this, "마커 1 클릭", Toast.LENGTH_SHORT).show();
                // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
                return true;
            }
        });

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
