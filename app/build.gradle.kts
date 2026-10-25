import java.security.MessageDigest

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "app.homedl"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.homedl"
        minSdk = 29
        targetSdk = 35
        versionCode = 13
        versionName = "0.2.6"
        ndk { abiFilters += "arm64-v8a" }
    }
    signingConfigs {
        if (!System.getenv("HOME_DL_KEYSTORE").isNullOrBlank()) {
            create("permanent") {
                storeType = "PKCS12"
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
    implementation("androidx.core:core:1.15.0")
    implementation("androidx.webkit:webkit:1.12.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    implementation("io.github.junkfood02.youtubedl-android:library:0.18.1")
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1")
}

// A local build must run the same verified preparation as CI; never ship an absent asset.
val verifyEngineAsset by tasks.registering {
    doLast {
        val asset = file("src/main/assets/engine/yt-dlp")
        check(asset.isFile) { "Run python3 scripts/prepare_engine.py from the repository root first." }
        val actual = MessageDigest.getInstance("SHA-256").digest(asset.readBytes())
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        check(actual == "b7af77f686e95f7845ce8d09d21957718febb8b48b30555adb9dd2292ad7bf14") {
            "Bundled engine checksum mismatch"
        }
    }
}
tasks.named("preBuild") { dependsOn(verifyEngineAsset) }

tasks.register<JavaExec>("probePublicInstagram") {
    dependsOn("testDebugUnitTest")
    classpath = tasks.named<Test>("testDebugUnitTest").get().classpath
    mainClass.set("app.homedl.PublicInstagramProbe")
}
