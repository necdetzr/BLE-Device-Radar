plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.android.library.compose)
}

android {
    namespace = "com.necdetzr.navigation"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewModel.navigation3)
}
