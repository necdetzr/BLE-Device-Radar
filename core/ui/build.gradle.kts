plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.android.library.compose)
}

android {
    namespace = "com.necdetzr.ui"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
}
