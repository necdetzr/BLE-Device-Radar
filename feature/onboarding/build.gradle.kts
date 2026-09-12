plugins {
    alias(libs.plugins.bledeviceradar.android.feature)
    alias(libs.plugins.bledeviceradar.android.library.compose)
}

android {
    namespace = "com.necdetzr.onboarding"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(project(":core:navigation"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
}
