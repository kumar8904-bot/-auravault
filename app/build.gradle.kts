plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ati.leadhunter.v54"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ati.leadhunter.v54"
        minSdk = 26
        targetSdk = 35
        versionCode = 54
        versionName = "5.4.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}
