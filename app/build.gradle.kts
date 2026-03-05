plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.musicplayerapp"
    compileSdk = 36

    buildFeatures{
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.musicplayerapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    val exoplayerVersion = "1.3.1"
    val glideVersion = "4.15.1"
    val navigationComponentVersion = "2.7.7"

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    implementation("com.google.android.exoplayer:exoplayer:$exoplayerVersion")

    implementation("androidx.media3:media3-exoplayer:$exoplayerVersion")
    implementation("androidx.media3:media3-session:$exoplayerVersion")
    implementation("androidx.media3:media3-ui:$exoplayerVersion")

    implementation("com.github.bumptech.glide:glide:$glideVersion")
    annotationProcessor("com.github.bumptech.glide:glide:$glideVersion")

    implementation("androidx.palette:palette:1.0.0")
//    implementation("com.github.alexei-frolo:WaveformSeekBar:1.1")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationComponentVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationComponentVersion")

    // Splash API
    implementation("androidx.core:core-splashscreen:1.0.1")
}