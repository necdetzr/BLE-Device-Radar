plugins {
    alias(libs.plugins.bledeviceradar.android.feature)
    alias(libs.plugins.bledeviceradar.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.necdetzr.history"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(project(":core:navigation"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    testImplementation(project(":core:testing"))
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
