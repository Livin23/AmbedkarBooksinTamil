package com.livin.ambedkarindhiavilsathigal.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

private const val INTERSTITIAL_ID = "ca-app-pub-7765068854860263/1610241938"

class AdManager(private val context: Context) {

    private var interstitial: InterstitialAd? = null

    fun preload() {
        if (interstitial != null) return
        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitial = null }
            }
        )
    }

    fun showIfReady(activity: Activity, onFinished: () -> Unit) {
        val ad = interstitial
        if (ad == null) {
            onFinished()
            preload()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                preload()
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                interstitial = null
                preload()
                onFinished()
            }
        }
        ad.show(activity)
    }
}
