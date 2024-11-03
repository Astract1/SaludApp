    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.jetbrains.kotlin.android)
        id("kotlin-kapt")
        id("com.google.gms.google-services")
        id("androidx.navigation.safeargs.kotlin")

    }

    android {
        namespace = "com.astract.saludapp"
        compileSdk = 34

        buildFeatures {
            buildConfig = true
            viewBinding = true
        }

        defaultConfig {
            applicationId = "com.astract.saludapp"
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
                buildConfigField("String", "NEWS_API_KEY", "\"${project.findProperty("NEWS_API_KEY") ?: "default_key"}\"")
            }
            debug {
                buildConfigField("String", "NEWS_API_KEY", "\"${project.findProperty("NEWS_API_KEY") ?: "default_key"}\"")
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
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation("com.google.android.material:material:1.12.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
        implementation("androidx.navigation:navigation-fragment:2.8.0")
        implementation("androidx.navigation:navigation-ui:2.8.0")
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)
        implementation(libs.androidx.fragment)
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.viewpager2:viewpager2:1.1.0")
        implementation("com.github.bumptech.glide:glide:4.16.0")
        implementation(libs.firebase.firestore.ktx)
        kapt("com.github.bumptech.glide:compiler:4.16.0")
        kapt("androidx.room:room-runtime:2.6.1")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
        implementation(libs.androidx.foundation.android)
        implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
        implementation(("com.google.android.gms:play-services-auth:21.2.0"))
        implementation("com.google.firebase:firebase-firestore-ktx")
    }

