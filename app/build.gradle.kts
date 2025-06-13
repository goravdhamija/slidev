plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.globewaystechnologies.slidevideospy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.globewaystechnologies.slidevideospy"
        minSdk = 30
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:x.x.x")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:x.x.x")

    val camerax_version = "1.5.0-beta01"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-mlkit-vision:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")
    implementation("com.android.billingclient:billing:7.0.0")


    val nav_version = "2.9.0"
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.compose.material:material:1.8.2")
    implementation("androidx.compose.material3:material3:1.2.0")


    implementation("androidx.compose.ui:ui-text-google-fonts:1.8.2")
    implementation("androidx.compose.ui:ui-graphics:1.8.2")



    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0") //
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.8.2")

    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.7")
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.1.7")
    implementation("androidx.datastore:datastore-preferences-core:1.1.7")
    implementation("androidx.datastore:datastore:1.1.7")
    implementation("androidx.datastore:datastore-rxjava2:1.1.7")
    implementation("androidx.datastore:datastore-rxjava3:1.1.7")
    implementation("androidx.datastore:datastore-core:1.1.7")

    implementation("androidx.compose.material:material-icons-extended:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("io.coil-kt:coil-compose:2.5.0")


}