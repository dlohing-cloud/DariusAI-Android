plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.dariusai"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.dariusai"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "3.0"
    }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
