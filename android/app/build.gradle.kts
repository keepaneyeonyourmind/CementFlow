plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val useUsbServer = providers.gradleProperty("cementflowUsb").orNull == "true"

android {
    namespace = "com.example.cementflow"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.cementflow"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DEMO_API_URL", "\"http://10.0.2.2:8080\"")
    }

    buildTypes {
        debug {
            if (useUsbServer) {
                buildConfigField("String", "DEMO_API_URL", "\"http://127.0.0.1:8080\"")
            }
        }
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
