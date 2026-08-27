plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.android.library.compose)
}

android {
    namespace = "com.necdetzr.designsystem"
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)

    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3.navigationSuite)
    implementation(libs.androidx.compose.material.iconsExtended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
