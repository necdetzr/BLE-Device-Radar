plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.android.library.compose)
}

android {
    namespace = "com.necdetzr.designsystem"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    api(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
