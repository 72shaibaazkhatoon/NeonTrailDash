package com.neontraildash.game

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : Activity() {
    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null
    private lateinit var game: GameView
    private var completedRuns = 0

    // Google-provided TEST ad unit IDs. Replace only after registering your app in AdMob.
    private val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"
    private val testRewardedId = "ca-app-pub-3940256099942544/5224354917"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 15, 39)
        window.navigationBarColor = Color.rgb(12, 15, 39)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        game = GameView(this,
            onRewardRequested = { showRewarded() },
            onRunFinished = {
                completedRuns++
                if (completedRuns % 3 == 0) showInterstitial()
            })
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(12, 15, 39))
            addView(game, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        })

        MobileAds.initialize(this) {
            loadInterstitial()
            loadRewarded()
        }
    }

    private fun loadInterstitial() {
        InterstitialAd.load(this, testInterstitialId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { interstitial = null }
            })
    }

    private fun showInterstitial() {
        val ad = interstitial ?: run { loadInterstitial(); return }
        ad.show(this)
        interstitial = null
        loadInterstitial()
    }

    private fun loadRewarded() {
        RewardedAd.load(this, testRewardedId, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewarded = ad; game.invalidate() }
                override fun onAdFailedToLoad(error: LoadAdError) { rewarded = null; game.invalidate() }
            })
    }

    private fun showRewarded() {
        val ad = rewarded
        if (ad == null) {
            Toast.makeText(this, "Reward ad is loading. Please try again.", Toast.LENGTH_SHORT).show()
            loadRewarded()
            return
        }
        rewarded = null
        ad.show(this) { reward ->
            game.addBonusCoins(reward.amount.toInt().coerceAtLeast(50))
        }
        loadRewarded()
    }

    override fun onResume() { super.onResume(); if (::game.isInitialized) game.resumeGame() }
    override fun onPause() { if (::game.isInitialized) game.pauseGame(); super.onPause() }
}
