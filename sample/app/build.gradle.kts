import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    id("io.github.5peak2me.gradle.elf-16k-alignment")
}

android {
    namespace = "com.example.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a"/*, "armeabi-v7a"*/, "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.map.amap.location.search)

    implementation(libs.map.baidu)
    implementation(libs.map.baidu.location)
    implementation(libs.map.baidu.base)
    implementation(libs.map.baidu.util)

    implementation(libs.map.tencent)
    implementation(libs.map.tencent.utilities)
    implementation(libs.map.tencent.foundation)
    implementation(libs.map.tencent.geolocation)
}

elfAlignment {
    maxAlign.set(16384L)
    resolveOnBuild.set(true)
    output {
        html.set(true)
        json.set(true)
    }
}
