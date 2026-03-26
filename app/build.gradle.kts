plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

import java.util.Properties
import java.io.FileInputStream
import java.io.File


android {
    namespace = "com.lifecalendar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lifecalendar"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Apply signing config if properties are provided
            if (project.hasProperty("keystoreProperties")) {
                val propsPath = project.property("keystoreProperties") as String
                val propsFile = file(propsPath)
                if (propsFile.exists()) {
                    val props = Properties()
                    propsFile.inputStream().use { props.load(it) }
                    
                    val signingConfig = signingConfigs.create("release") {
                        val storeFilePath = props.getProperty("storeFile")
                        val potentialJks = File(propsFile.parentFile, storeFilePath)
                        val appJks = File(File(propsFile.parentFile, "app"), storeFilePath)
                        
                        storeFile = when {
                            File(storeFilePath).isAbsolute -> File(storeFilePath)
                            potentialJks.exists() -> potentialJks
                            appJks.exists() -> appJks
                            else -> potentialJks // Fallback to current behavior
                        }
                        
                        storePassword = props.getProperty("storePassword")
                        keyAlias = props.getProperty("keyAlias")
                        keyPassword = props.getProperty("keyPassword")
                    }

                    this.signingConfig = signingConfig
                }
            }


        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // WorkManager for scheduling weekly wallpaper updates
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
