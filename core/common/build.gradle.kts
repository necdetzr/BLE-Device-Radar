plugins {
    alias(libs.plugins.bledeviceradar.android.library)
    alias(libs.plugins.bledeviceradar.hilt)
}

android {
    namespace = "com.necdetzr.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.junit)
}
