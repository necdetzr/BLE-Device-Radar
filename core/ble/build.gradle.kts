plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.hilt)
}

android {
    namespace = "com.necdetzr.ble"
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
