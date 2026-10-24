plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "app.homedl"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.homedl"
        minSdk = 29
        targetSdk = 35
        versionCode = 4
        versionName = "0.1.3-retro-future"
        ndk { abiFilters += "arm64-v8a" }
    }
    signingConfigs {
        if (!System.getenv("HOME_DL_KEYSTORE").isNullOrBlank()) {
            create("permanent") {
                storeFile = file(System.getenv("HOME_DL_KEYSTORE"))
                storePassword = System.getenv("HOME_DL_STORE_PASSWORD")
                keyAlias = System.getenv("HOME_DL_KEY_ALIAS")
                keyPassword = System.getenv("HOME_DL_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            signingConfigs.findByName("permanent")?.let { signingConfig = it }
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    packaging { jniLibs { useLegacyPackaging = true } }
}
dependencies {
    implementation("io.github.junkfood02.youtubedl-android:library:0.18.1")
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1")
}
