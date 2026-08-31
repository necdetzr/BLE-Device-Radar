plugins {
    alias(libs.plugins.bledeviceradar.android.feature)
    alias(libs.plugins.bledeviceradar.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.necdetzr.radar"
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.animation)
    implementation(project(":core:navigation"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.androidx.activity.compose)
    testImplementation(project(":core:testing"))
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
