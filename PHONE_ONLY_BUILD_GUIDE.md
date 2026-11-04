# Phone-only cloud build guide

This project can be compiled from a phone using GitHub Actions in a browser. You do not need Android Studio installed on your phone.

1. Download and extract the ZIP on Android.
2. In Chrome, sign in to GitHub and create a **private** repository named `NeonTrailDash`.
3. Use GitHub's web upload interface to upload the *contents* of the extracted project (including hidden `.github/workflows/android-build.yml`). Make sure the root of the repository contains `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, and the `app` folder. If GitHub's mobile upload interface cannot preserve folders/hidden files, switch Chrome to Desktop site or use github.dev's file explorer.
4. Open the repository's **Actions** tab. Select **Build Neon Trail Dash Android App Bundle**, then choose **Run workflow**.
5. Wait for the workflow to finish. Open the completed run and download the `NeonTrailDash-release-AAB` artifact. It is a ZIP containing `app-release.aab`.
6. Keep the AAB private. It is an unsigned-for-Play-upload release bundle at this stage only if Android Gradle Plugin signing config is not set; configure Play App Signing/upload key through a secure signing workflow before production. Never put passwords or signing keys in a public repository.

If the workflow fails, open the run and copy the first red error section for troubleshooting.

## Important
- GitHub Actions is a cloud build route, not a guarantee the app is production-ready.
- Before a public release, replace sample AdMob IDs, implement consent/privacy messaging and verify data-safety declarations.
- This initial project uses Google sample test ad IDs. Test ads do not generate earnings.
- The project needs device testing, policy review, final store graphics and a signed release process.
