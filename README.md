# Neon Trail Dash — Android starter project

An original native Android endless-runner prototype written in Kotlin and Android Canvas.

## Included
- Landscape Android game screen
- Tap-to-jump runner gameplay
- Random obstacles and collectible coins
- Score, best score and coin balance saved locally
- Game-over screen and restart
- Optional rewarded-ad button (+50 coins)
- Interstitial ad shown at a natural break after every third run
- Google Mobile Ads SDK dependency and Google TEST ad unit IDs

## Open and build
1. Install Android Studio with Android SDK Platform 36 and JDK 17.
2. Extract this folder.
3. In Android Studio choose **Open** and select this folder.
4. Allow Gradle to sync; an internet connection is needed for dependencies.
5. Run on an Android device or emulator in landscape mode.
6. For a release bundle choose **Build → Generate Signed Bundle / APK → Android App Bundle**.

This source package does not include a compiled APK/AAB, signing key, Play Console account, or your personal AdMob account.

## Ads — important before release
The project currently uses Google's sample TEST App ID and TEST ad-unit IDs. They do not earn revenue.
1. Create/register the app in Google AdMob.
2. Replace the sample App ID in `app/src/main/AndroidManifest.xml`.
3. Replace the two test ad-unit IDs in `MainActivity.kt` with your own interstitial and rewarded ad-unit IDs.
4. Keep test ads enabled while developing. Never click your own live ads.
5. Add and configure Google's User Messaging Platform (UMP) consent flow and any required privacy/child-directed treatment before serving ads. Complete AdMob and Play Console data-safety disclosures accurately.
6. Do not publish until real ad configuration and privacy disclosures have been reviewed.

## Release checks
- Use a unique final application ID if `com.neontraildash.game` is already taken.
- Verify the current Play Console target API requirement before upload. This project targets API 36.
- Test on several real Android devices and screen sizes.
- Check audio, performance, pause/resume, offline behavior, crashes and ad frequency.
- Create a signed AAB and keep the upload key safe.
- Prepare store icon, feature graphic, screenshots, app description, content rating, Data safety form, ads declaration and privacy policy.
- If the game is directed to children or is likely to attract children, review Google Play Families and AdMob child-directed requirements before release. Do not misstate the target audience.

## Current status
This is a functional starter implementation, not a completed commercial game or a guarantee of Play Store approval. It needs device testing, visual/audio polish, consent implementation, final AdMob IDs, signing and store review.
