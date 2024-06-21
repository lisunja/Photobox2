plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.photobox2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.photobox2"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {
    implementation(libs.service)
    implementation(libs.smb)
    implementation(libs.slf4j)
    implementation(libs.slf4j.log)
    implementation(libs.tensorflow)
    implementation(libs.ai)
    implementation(libs.scanner)
    implementation(libs.crop)
    implementation(libs.camera.core)
    implementation(libs.gms)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.video)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)
    implementation(libs.zxing)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}