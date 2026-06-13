import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val keystoreProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.exists()) {
        load(FileInputStream(propertiesFile))
    }
}

val versionProperties = Properties().apply {
    val propertiesFile = rootProject.file("version.properties")
    if (propertiesFile.exists()) {
        load(FileInputStream(propertiesFile))
    }
}

android {
    namespace = "com.chumakov123.udaw"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chumakov123.udaw"
        minSdk = 24
        targetSdk = 34
        versionCode = versionProperties["versionCode"]?.toString()?.toInt() ?: 1
        versionName = versionProperties["versionName"]?.toString() ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++2a"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProperties["storeFile"]?.toString()?.let { file(it) }
                ?: System.getenv("RELEASE_STORE_FILE")?.let { file(it) }
            storePassword = keystoreProperties["storePassword"]?.toString() ?: System.getenv("RELEASE_STORE_PASSWORD")
            keyAlias = keystoreProperties["keyAlias"]?.toString() ?: System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = keystoreProperties["keyPassword"]?.toString() ?: System.getenv("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appLabel"] = "U-DAW (Debug)"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["appLabel"] = "U-DAW"
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }

            externalNativeBuild {
                cmake {
                    // Enable LTO for release builds
                    arguments += "-DANDROID_CPP_FEATURES=rtti exceptions"
                    cppFlags += "-O3 -flto"
                }
            }
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
        prefab = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.documentfile)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.oboe)
}
