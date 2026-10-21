plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.taly.toss.devices.hcs350"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:projector-control"))
    implementation(project(":core:capabilities"))
}
