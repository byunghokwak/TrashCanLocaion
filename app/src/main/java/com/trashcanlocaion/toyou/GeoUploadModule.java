package com.trashcanlocaion.toyou;

import android.content.res.Resources;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GeoUploadModule {
    private final int ID = 0;
    private final int LOCATION_SET = 1;
    private final int LOCATION_DETAILS = 2;
    private final int GEOMETRY = 3;

    private final int LATITUDE = 0;
    private final int LONGITUDE = 1;

    private Resources resources;
    private ArrayList<GeoDB> geoDBArrayList;
    private InputStreamReader is;
    private DocumentReference documentReference;

    private GeoDB geoDB;
    private String geoPoint;
    private Map<String, Object> locationMap;

    GeoUploadModule(Resources resources, DocumentReference documentReference) {
        this.resources = resources;
        this.documentReference = documentReference;
        geoDBArrayList = new ArrayList<>();
        locationMap = new HashMap<>();
    }

    // 1. 엑셀/csv 정보 불러오기
    // 2. 위 정보를 array에 담기
    // 3. 담긴 정보를 firebase db에 저장시키기

    public void start() {
        try {
            is = new InputStreamReader(resources.openRawResource(R.raw.seoul_trashcan_location_v1), "euc-kr");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(is);
        CSVReader csvReader = new CSVReader(reader);
        String[] record = null;

        try {
            while((record = csvReader.readNext()) != null) {
                String geoString = record[GEOMETRY];
                String geoInfo[] = geoString.split(", ");

                geoDB = new GeoDB(record[ID], record[LOCATION_SET], record[LOCATION_DETAILS],
                        Double.parseDouble(geoInfo[LATITUDE]), Double.parseDouble(geoInfo[LONGITUDE]));
                geoDBArrayList.add(geoDB);

                geoPoint = record[GEOMETRY];
                String key = geoDB.getId() + "+" + geoDB.getLocation();
                locationMap.put(key, Arrays.asList(geoDB.getLocation(), geoDB.getLocationDetails(), geoPoint));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }

        documentReference.update(locationMap);
    }
}

class GeoDB {
    private String id;
    private double latitude;   // 위도
    private double longitude; // 경도
    private String location;
    private String locationDetails;

    GeoDB(String id, String location, String locationDetails,
          double latitude, double longitude) {
        this.id = id;
        this.location = location;
        this.locationDetails = locationDetails;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return this.id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocationDetails() {
        return locationDetails;
    }

    public String getLocation() {
        return location;
    }
}
