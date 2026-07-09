plugins {
    alias(libs.plugins.bledeviceradar.android.feature)
    alias(libs.plugins.bledeviceradar.android.library.compose)

    alias(libs.plugins.bledeviceradar.hilt)
}

android {
    namespace = "com.necdetzr.settings"

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation3.runtime)
    implementation(project(":core:navigation"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.compose.material3)
}
