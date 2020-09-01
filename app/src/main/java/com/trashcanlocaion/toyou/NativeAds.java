package com.trashcanlocaion.toyou;

import android.os.Bundle;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import androidx.appcompat.app.AppCompatActivity;

public class NativeAds extends AppCompatActivity {
    private AdLoader adLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.native_ad_mediumsize);

//        AdLoader.Builder adLoader = new AdLoader.Builder(
//                this, "ca-app-pub-3940256099942544/2247696110");
//
//        adLoader.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
//            @Override
//            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
//                TemplateView template = findViewById(R.id.my_template);
//                template.setNativeAd(unifiedNativeAd);
//            }
//        });

        adLoader = new AdLoader.Builder(getApplicationContext(), getString(R.string.native_ad_unit_id_for_test))
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        TemplateView template = findViewById(R.id.my_template);
                        template.setNativeAd(unifiedNativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    // Handle the failure by logging, altering the UI, and so on.
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        AdRequest adRequest = new AdRequest.Builder().build();
        adLoader.loadAd(adRequest);
    }
}
