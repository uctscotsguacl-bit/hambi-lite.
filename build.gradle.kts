plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "uz.demo.hambilite"
    compileSdk = 35

    defaultConfig {
        applicationId = "uz.demo.hambilite"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
    }
}
