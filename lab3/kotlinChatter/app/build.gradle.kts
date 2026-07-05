plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

android {
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    namespace = "cn.edu.sjtu.Yuki666.kotlinChatter"
    compileSdk = 35

    defaultConfig {
        applicationId = "cn.edu.sjtu.Yuki666.kotlinChatter"
        minSdk = 33
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("io.coil-kt:coil:1.3.2")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.5")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
